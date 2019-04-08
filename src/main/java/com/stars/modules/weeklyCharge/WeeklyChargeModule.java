package com.stars.modules.weeklyCharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.vip.VipModule;
import com.stars.modules.weeklyCharge.packet.ClientWeeklyCharge;
import com.stars.modules.weeklyCharge.prodata.WeeklyChargeVo;
import com.stars.modules.weeklyCharge.userdata.RoleWeeklyChargeVo;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargeModule extends AbstractModule implements OpActivityModule {

    /**
     * 角色周累计充值实体类
     */
    private RoleWeeklyChargeVo roleWeeklyChargeVo;


    public WeeklyChargeModule(long roleId, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("WeeklyCharge", roleId, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
//        if(isOpenActivity()){
        //周累计活动已经开始，为玩家初始化数据
        String sqlRoleWeeklyCharge = "select * from roleweeklycharge where roleid = " + id();
        roleWeeklyChargeVo = DBUtil.queryBean(DBUtil.DB_USER, RoleWeeklyChargeVo.class, sqlRoleWeeklyCharge);
//        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        if (roleWeeklyChargeVo == null) {
            RoleModule roleModule = module(MConst.Role);
            Role role = roleModule.getRoleRow();
            VipModule vipModule = module(MConst.Vip);
            roleWeeklyChargeVo = new RoleWeeklyChargeVo(id());
            roleWeeklyChargeVo.setWeeklyLevel(role.getLevel());
            roleWeeklyChargeVo.setWeeklyVipLevel(vipModule.getVipLevel());
            roleWeeklyChargeVo.setCurActId(OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyCharge));
            context().insert(roleWeeklyChargeVo);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
//    	if(!isOpenActivity()){
//    		return;
//    	}
        if (roleWeeklyChargeVo == null) {
            RoleModule roleModule = module(MConst.Role);
            Role role = roleModule.getRoleRow();
            VipModule vipModule = module(MConst.Vip);
            roleWeeklyChargeVo = new RoleWeeklyChargeVo(id());
            roleWeeklyChargeVo.setWeeklyLevel(role.getLevel());
            roleWeeklyChargeVo.setWeeklyVipLevel(vipModule.getVipLevel());
            roleWeeklyChargeVo.setCurActId(OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyCharge));
            context().insert(roleWeeklyChargeVo);
        }
    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    @Override
    public int getCurShowActivityId() {
        if (isOpenActivity())
            return OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyCharge);
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    /**
     * 活动是否开启
     *
     * @return
     */
    public boolean isOpenActivity() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WeeklyCharge);
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (operateActVo != null) {
            //判断角色属性限制
            if (operateActivityModule.isShow(operateActVo.getRoleLimitMap())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 周累计充值发放奖励
     *
     * @param money
     */
    public void handleChargeEvent(int money) {
        if (!isOpenActivity()) return;
        roleWeeklyChargeVo.addTotalCharge(money);
        context().update(roleWeeklyChargeVo);

        List<WeeklyChargeVo> list = getWeeklyTotalChargeListFilterLevelAndVipLv();
        if (StringUtil.isEmpty(list)) return;

        int curCharge = roleWeeklyChargeVo.getRoleTotalCharge();
        for (WeeklyChargeVo vo : list) {
            if (curCharge >= vo.getTotalCharge() && !roleWeeklyChargeVo.hasSendAward(vo.getWeeklyTotalId())) {
                sendAward(vo);
            }
        }
    }

    /**
     * 发放奖励（通过邮件发放）
     *
     * @param vo
     */
    private void sendAward(WeeklyChargeVo vo) {
        if (vo == null) return;
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(vo.getReward(), 1, true);
        ServiceHelper.emailService().sendToSingle(id(), WeeklyChargeManager.MAIL_ID, 0l, "系统", map, String.valueOf(vo.getTotalCharge()));

        roleWeeklyChargeVo.recordSendAward(vo.getWeeklyTotalId());
        context().update(roleWeeklyChargeVo);
    }

    /**
     * 获取根据玩家初始化的等级和vip等级筛选后的周累计充值奖励数据
     *
     * @return
     */
    private List<WeeklyChargeVo> getWeeklyTotalChargeListFilterLevelAndVipLv() {
        List<WeeklyChargeVo> list = new ArrayList<>();
        for (WeeklyChargeVo vo : WeeklyChargeManager.weeklyChargeInfoMap.values()) {
            if (vo.matchLevel(roleWeeklyChargeVo.getWeeklyLevel())
                    && vo.matchVipLevel(roleWeeklyChargeVo.getWeeklyVipLevel())) {
                list.add(vo);
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * 下发周累计充值奖励数据给客户端
     */
    public void viewMainUI() {
        if (!isOpenActivity()) {
            warn(I18n.get("marry.wedding.inactivity"));
            return;
        }

        ClientWeeklyCharge client = new ClientWeeklyCharge();
        client.setRoleTotalCharge(roleWeeklyChargeVo.getRoleTotalCharge());
        List<WeeklyChargeVo> weeklyChargeVoList = getWeeklyTotalChargeListFilterLevelAndVipLv();
        client.setList(weeklyChargeVoList);
        send(client);
    }

    public void onReset() {
        try {
            onDataReq();
            onInit(false);
        } catch (Throwable throwable) {
            LogUtil.error("actLoopReset:4007 fail" + id(), throwable);
        }
    }
}

