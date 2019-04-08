package com.stars.modules.escort;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.escort.event.*;
import com.stars.modules.escort.listener.EscortEventListener;
import com.stars.modules.escort.prodata.CargoAIVo;
import com.stars.modules.escort.prodata.CargoCarVo;
import com.stars.modules.escort.prodata.CargoMonsterVo;
import com.stars.services.activities.ActConst;
import com.stars.util.DateUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class EscortModuleFactory extends AbstractModuleFactory<EscortModule> {
    public EscortModuleFactory() {
        super(new EscortPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        //初始化镖车数据
        initCargoProductData();
        //初始化镖车AI数据
        initCargoAiProductData();
        //初始化运镖怪物数据
        initCargoMonsterProductData();
        //初始化配置数据
        initConfig();
        //初始化活动流程
        initAcitityFlow();
    }

    //初始化活动流程
    private void initAcitityFlow() throws Exception{
        EscortActivityFlow activityFlow = new EscortActivityFlow();
        activityFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_ESCORT));
        EscortManager.activityFlow = activityFlow;
    }

    //初始化镖车数据
    private void initCargoProductData() throws SQLException {
        String sql = "select * from cargocar";
        Map<Integer, CargoCarVo> cargoCarVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "carid", CargoCarVo.class, sql);
        EscortManager.setCargoCarVoMap(cargoCarVoMap);
    }

    //初始化运镖怪物数据
    private void initCargoMonsterProductData() throws SQLException {
        String sql = "select * from cargomonster";
        List<CargoMonsterVo> cargoMonsterVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, CargoMonsterVo.class, sql);
        EscortManager.setCargoMonsterVoList(cargoMonsterVoList);
    }

    //初始化镖车AI数据
    private void initCargoAiProductData() throws SQLException {
        String sql = "select * from cargoai";
        List<CargoAIVo> cargoAiVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, CargoAIVo.class, sql);
        EscortManager.setCargoAiVoList(cargoAiVoList);
    }

    //初始化配置数据
    private void initConfig() throws Exception {
        String lvStr = DataManager.getCommConfig("cargocar_teamreqlv");
        String[] lvStrData = lvStr.split("\\+");
        EscortManager.setOpenLevel(Integer.parseInt(lvStrData[0]));     //运镖系统开启等级
        EscortManager.setTeamModeLevel(Integer.parseInt(lvStrData[1])); //组队模式开启等级

        EscortManager.setCargocarSafeid(DataManager.getCommConfig("cargocar_safeid",0));    //镖车列队场景
        EscortManager.setCargocarStageid(DataManager.getCommConfig("cargocar_stageid", 0)); //运镖和战斗场景
        EscortManager.setCargocarDailyRefreshCount(DataManager.getCommConfig("cargocar_refreshcount", 2));

        //vip等级对应可邀请队员数量
        LinkedHashMap<Integer,Integer> teamMemberCountLimit = new LinkedHashMap<>();
        String memberStr = DataManager.getCommConfig("cargocar_teammember");
        String[] memberStrData = memberStr.split("\\|");
        for(String strData:memberStrData){
            String[] innerArray = strData.split("\\+");
            teamMemberCountLimit.put(Integer.parseInt(innerArray[0]),Integer.parseInt(innerArray[1]));
        }
        EscortManager.setTeamMemberCountLimit(teamMemberCountLimit);

        //用于计算和获取镖车
        EscortManager.setCargocarCoefficient(DataManager.getCommConfig("cargocar_coefficient", 0));
        //镖车队列场景初始取车上限
        EscortManager.setCargocarCarcount(DataManager.getCommConfig("cargocar_carcount", 0));

        String dayCountStr = DataManager.getCommConfig("cargocar_daycount");
        String[] dayCountStrData = dayCountStr.split("\\+");
        EscortManager.setCargocarDayCount(Integer.parseInt(dayCountStrData[0]));//每天运镖次数
        EscortManager.setCargocarRobCount(Integer.parseInt(dayCountStrData[1]));//每天劫镖次数

        //镖车被劫后的保护时间
        EscortManager.setCargocarProtectTime(DataManager.getCommConfig("cargocar_protecttime", 0));
        //仇人关系维持的时间
        EscortManager.setCargocarEnemyTime(DataManager.getCommConfig("cargocar_enemytime", 0) * DateUtil.HOUR);
        //buffid  表示劫镖队伍攻击仇人运镖队伍时附加的buff
        String cargocar_enemydamage = DataManager.getCommConfig("cargocar_enemydamage");
        String[] buffData = cargocar_enemydamage.split("\\+");
        EscortManager.setCargocarEnemyBuffId(Integer.parseInt(buffData[0]));
        EscortManager.setCargocarEnemyBuffLevel(Integer.parseInt(buffData[1]));
        //使用面具劫镖时，消耗多少个面具
        String maskStr = DataManager.getCommConfig("cargocar_mask");
        String[] maskStrData = maskStr.split("\\+");
        EscortManager.setCargocarMaskItemid(Integer.parseInt(maskStrData[0]));
        EscortManager.setCargocarMask(Integer.parseInt(maskStrData[1]));

        String refreshStr = DataManager.getCommConfig("cargocar_refreshreq");
        String[] refreshStrData = refreshStr.split("\\+");
        EscortManager.setCargocarRefreshItemid(Integer.parseInt(refreshStrData[0]));
        EscortManager.setCargocarRefreshCount(Integer.parseInt(refreshStrData[1]));

        String loseCargoStr = DataManager.getCommConfig("cargocar_losecargo");
        String[] loseCargoStrData = loseCargoStr.split("\\+");
        Map<Integer,Integer> loseCargoMap = new HashMap<>();
        loseCargoMap.put(1, Integer.parseInt(loseCargoStrData[0]));
        loseCargoMap.put(2, Integer.parseInt(loseCargoStrData[1]));
        EscortManager.setCargocarLoseCargoMap(loseCargoMap);//被劫镖损失的货物比例

        //秒为单位，表示每次劫镖战斗持续的时间，超时算劫镖失败
        EscortManager.setCargocarFightTime(DataManager.getCommConfig("cargocar_fighttime", 60));
        //百分比，表示所有奖励如果不是在活动时间段内进行，则乘以此百分比
        EscortManager.setCargocarDouble(DataManager.getCommConfig("cargocar_double", 200));

        String getCarStr = DataManager.getCommConfig("cargocar_getcarcoefficient");
        String[] getCarStrData = getCarStr.split("\\|");
        String[] tmp;
        Map<Integer,int[]> getCarCoef = new HashMap<>();
        int carId;
        int min,max;
        for(String str:getCarStrData){
            tmp = str.split("\\+");
            carId = Integer.parseInt(tmp[0]);
            min = Integer.parseInt(tmp[1]);
            max = Integer.parseInt(tmp[2]);
            getCarCoef.put(carId,new int[]{min,max});
        }
        EscortManager.setCargoGetCarCountCoef(getCarCoef);

        //镖车根据顺序逐个移动向寻路点
        String movePositionStr = DataManager.getCommConfig("cargocar_moveposition");
        String[] movePositionStrData = movePositionStr.split("\\|");
        EscortManager.setMovePositionData(movePositionStrData);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public EscortModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new EscortModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        EscortEventListener listener = new EscortEventListener(module);

        eventDispatcher.reg(EnterEscortSceneEvent.class, listener);
        eventDispatcher.reg(EnterEscortSafeSceneEvent.class, listener);
        eventDispatcher.reg(CheckRobTimesBackCityEvent.class, listener);
        eventDispatcher.reg(EnterCargoListSceneEvent.class, listener);
        eventDispatcher.reg(NoticeServerAddEnemyRecordEvent.class, listener);
        eventDispatcher.reg(NoticeServerAddEscortAwardEvent.class, listener);
        eventDispatcher.reg(NoticeConsumeMaskEvent.class, listener);
    }
}
