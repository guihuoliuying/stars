package com.stars.modules.role;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newequipment.NewEquipmentConstant;
import com.stars.modules.role.event.FriendGetVigorEvent;
import com.stars.modules.role.event.ModifyRoleLevelEvent;
import com.stars.modules.role.event.ReduceRoleResourceEvent;
import com.stars.modules.role.gm.*;
import com.stars.modules.role.listener.*;
import com.stars.modules.role.prodata.FightScoreRewardVo;
import com.stars.modules.role.prodata.Grade;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.role.summary.RoleSummaryComponentImpl;
import com.stars.services.summary.Summary;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.data.DataManager.commonConfigMap;

@DependOn({MConst.Data})
public class RoleModuleFactory extends AbstractModuleFactory<RoleModule> {

    public RoleModuleFactory() {
        super(new RolePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        /* rolegrade */
        String rolegradeSelectSql = "select * from grade";
        List<Grade> rolegradeList = DBUtil.queryList(DBUtil.DB_PRODUCT, Grade.class, rolegradeSelectSql);
        RoleManager.setGradeDatas(rolegradeList);

        /* job */
        loadJobVo();
        /* resource */
        String resourceSelectSql = "select * from resource";
        List<Resource> resourceList = DBUtil.queryList(DBUtil.DB_PRODUCT, Resource.class, resourceSelectSql);
        RoleManager.setResourceDatas(resourceList);

        /* fightscorerewardvo */
//        loadFightScoreRewardVo();

        /* 体力恢复 */
        loadVigorRecovery();

        /* 体力购买 */
        loadVigorPrice();

        /* 体力可以储存的最大上限值 */
        loadVigorCanSaveMaxLimit();
    }

    @Override
    public void init() {
        GmManager.reg("setsave", new SaveJobGmHandler());
        GmManager.reg("addRoleExp", new AddRoleExpGmHandler());
        GmManager.reg("changerolejob", new ChangeJobGmHandler());
        GmManager.reg("setrolelevel", new SetRoleLevelGmHandler());
        GmManager.reg("attr", new AddAttributeGmHandler());

        Summary.regComponentClass(MConst.Role, RoleSummaryComponentImpl.class);
    }

    @Override
    public RoleModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new RoleModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(FriendGetVigorEvent.class, new FriendGetVigorListener((RoleModule) module));
        eventDispatcher.reg(ReduceRoleResourceEvent.class, new ReduceRoleResourceListener((RoleModule) module));
        eventDispatcher.reg(ModifyRoleLevelEvent.class, new ModifyRoleLevelListener((RoleModule) module));
        eventDispatcher.reg(ChangeJobEvent.class, new RoleChangeJobListenner((RoleModule) module));
        eventDispatcher.reg(RoleRenameEvent.class, new RoleRenameListenner((RoleModule) module));
    }

    private void loadJobVo() throws Exception {
        String jobSelectSql = "select * from job";
        List<Job> jobList = DBUtil.queryList(DBUtil.DB_PRODUCT, Job.class, jobSelectSql);
        Map<Integer, Job> jobMap = new HashMap<>();
        Job tmpJob = null;
        Map<Integer, Map<Byte, Integer>> bornEquipmentMap = new HashMap<>();
        String[] bornEquipArr = null;
        int jobId = 0;
        for (int i = 0, len = jobList.size(); i < len; i++) {
            tmpJob = jobList.get(i);
            jobId = tmpJob.getJobId();
            jobMap.put(jobId, tmpJob);
            bornEquipArr = tmpJob.getOriginequipment().split("\\|");
            if (bornEquipArr.length < NewEquipmentConstant.EQUIPMENT_MAX_COUNT) {
                throw new Exception("Job表配置的出生装备数据有问题: jobId=" + jobId);
            }
            for (byte k = 1; k <= NewEquipmentConstant.EQUIPMENT_MAX_COUNT; k++) {
                if (bornEquipmentMap.containsKey(jobId) == false) {
                    bornEquipmentMap.put(jobId, new HashMap<Byte, Integer>());
                }
                bornEquipmentMap.get(jobId).put(k, Integer.parseInt(bornEquipArr[k - 1]));
            }
            if (bornEquipArr.length < NewEquipmentConstant.EQUIPMENT_MAX_COUNT) {
                throw new Exception("Job表配置的解锁装备数据有问题: jobId=" + jobId);
            }
        }
        RoleManager.setJobDatas(jobMap);
        RoleManager.setBornEquipmentMap(bornEquipmentMap);
    }

    private void loadFightScoreRewardVo() throws Exception {
        String sql = "select * from `fightscorereward`; ";
        Map<Integer, FightScoreRewardVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "rewardid", FightScoreRewardVo.class, sql);
        RoleManager.fightScoreRewardVoMap = map;
    }

    private void loadVigorRecovery() {
        String recoveryStr = com.stars.util.MapUtil.getString(commonConfigMap, "vigor_recovery", "1+1");
        int recoveryInterval = Integer.parseInt(recoveryStr.split("\\+")[0]);
        int recoveryNumber = Integer.parseInt(recoveryStr.split("\\+")[1]);
        RoleManager.VIGOR_RECOVERY_INTERVAL = recoveryInterval * 1000;
        RoleManager.VIGOR_RECOVERY_NUMBER = recoveryNumber;
    }

    private void loadVigorPrice() {
        RoleManager.buyVigorLimit = Integer.parseInt(DataManager.getCommConfig("vigor_buy_limit"));
        RoleManager.vigorPriceMap = new HashMap<>();
        String str = MapUtil.getString(commonConfigMap, "vigor_buy", null);
        if (str == null) {
            return;
        }
        for (String priceStr : str.split("\\|")) {
            if ("".equals(priceStr.trim())) {
                continue;
            }
            String[] tuple = priceStr.split("\\+");
            RoleManager.vigorPriceMap.put(Integer.parseInt(tuple[0]), new int[]{
                    Integer.parseInt(tuple[1]), // itemId
                    Integer.parseInt(tuple[2]), // price
                    Integer.parseInt(tuple[3]), // buyPrice
            });
        }
    }

    private void loadVigorCanSaveMaxLimit() {
        RoleManager.canSaveMaxVigor = Integer.parseInt(DataManager.getCommConfig("vigor_max"));
    }

}