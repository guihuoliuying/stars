package com.stars.modules.dragonboat;

import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.dragonboat.event.DragonBoatHistorySendEvent;
import com.stars.modules.dragonboat.event.DragonBoatStatusChangeEvent;
import com.stars.modules.dragonboat.event.DragonBoatUpdateRankEvent;
import com.stars.modules.dragonboat.gm.DragonBoatGmHandler;
import com.stars.modules.dragonboat.listenner.DragonBoatSendAllRankListenner;
import com.stars.modules.dragonboat.listenner.DragonBoatStatusChangeListenner;
import com.stars.modules.dragonboat.listenner.DragonBoatUpdateRankListenner;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.modules.gm.GmManager;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.rank.RankManager;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatModuleFactory extends AbstractModuleFactory<DragonBoatModule> {
    public DragonBoatModuleFactory() {
        super(new DragonBoatPacketSet());
    }

    @Override
    public DragonBoatModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new DragonBoatModule("端午节活动：赛龙舟", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from dragonboat;";
        DragonBoatManager.dragonBoatMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "dragonboatid", DragonBoatVo.class, sql);
        List<Integer> dragonBoatFlowIds = StringUtil.toLinkedList(DataManager.getCommConfig("dragonboat_flowids"), Integer.class, '+');//活动流
        Map<Integer, Map<Long, Long>> flowTimeMap = new LinkedHashMap<>();
        Map<Integer, String> dayFlowMap = new HashMap<>();
        Map<Integer, Integer> dayActivityMap = new HashMap<>();
        int dayStep = 0;
        /**
         * 容灾过滤
         */
        for (int index = 0; index < dragonBoatFlowIds.size(); index++) {
            int activityId = dragonBoatFlowIds.get(index);
            Map<Long, Long> timeBetween = new HashMap<>();
            Map<Integer, String> activityFlowConfig = DataManager.getActivityFlowConfig(activityId);
            String beginCron = null;
            String endCron = null;
            for (int step = 1; step <= activityFlowConfig.size(); step++) {
                if (step == 1) {
                    beginCron = activityFlowConfig.get(step);
                }
                if (step == activityFlowConfig.size()) {
                    endCron = activityFlowConfig.get(step);
                }
            }
            long beginTime = ActivityFlowUtil.getTimeInMillisByCronExpr(beginCron);
            long endTime = ActivityFlowUtil.getTimeInMillisByCronExpr(endCron);
            timeBetween.put(1L, beginTime);
            timeBetween.put(0L, endTime);
            /**
             * 跨天的->淘汰
             */
            int crossDays = DateUtil.getRelativeDifferDays(new Date(beginTime), new Date(endTime));
            int crossdays2 = 0;
            /**
             * 不在同一天的->淘汰
             */
            if (flowTimeMap.size() > 1) {
                crossdays2 = DateUtil.getRelativeDifferDays(new Date(flowTimeMap.get(index - 1).get(1L)), new Date(beginTime));
            }
            /**
             * 时间交叉->淘汰
             */
            boolean notBetween = false;
            if (flowTimeMap.size() > 1) {
                notBetween = DateUtil.isBetweenTwoDays(new Date(beginTime), new Date(flowTimeMap.get(index - 1).get(1L)), new Date(flowTimeMap.get(index - 1).get(0L)));
            }
            if (crossDays < 1 && crossdays2 < 1 && !notBetween) {
                flowTimeMap.put(index, timeBetween);
                dayActivityMap.put(++dayStep, activityId);
            }
        }
        Map<Long, Integer> dayActivityTimeMap = new LinkedHashMap<>();
        int allStep = 1;
        for (Integer activityId : dayActivityMap.values()) {
            Map<Integer, String> activityFlowConfig = DataManager.getActivityFlowConfig(activityId);
            for (int step = 1; step <= activityFlowConfig.size(); step++) {
                String cron = activityFlowConfig.get(step);
                long time = ActivityFlowUtil.getTimeInMillisByCronExpr(cron);
                dayActivityTimeMap.put(time, step);
                dayFlowMap.put(allStep++, cron);
            }
        }
        Map<Integer, String> rankRewardMap = new HashMap<Integer, String>();
        List<RankAwardVo> rankAwardVoList = RankManager.getRankAward(DragonBoatConst.rankAwardId);
        for (RankAwardVo rankAwardVo : rankAwardVoList) {
            String section = rankAwardVo.getSection();
            String[] rankRange = section.split("\\+");
            if (rankRange[0].equals(rankRange[1])) {
                rankRewardMap.put(Integer.parseInt(rankRange[0]), rankAwardVo.getAward());
            } else {
                int rankMin = Integer.parseInt(rankRange[0]);
                int rankMax = Integer.parseInt(rankRange[1]);
                for (int rank = rankMin; rank <= rankMax; rank++) {
                    rankRewardMap.put(rank, rankAwardVo.getAward());
                }
            }
        }
        String dragonboatSpeedupRandomrange = DataManager.getCommConfig("dragonboat_speedup_randomrange");
        Integer[] dragonboatSpeedupRandomranges = StringUtil.toArray(dragonboatSpeedupRandomrange, Integer[].class, '+');
        String dragonboatSlowdownRandomrange = DataManager.getCommConfig("dragonboat_slowdown_randomrange");
        Integer[] dragonboatSlowdownRandomranges = StringUtil.toArray(dragonboatSlowdownRandomrange, Integer[].class, '+');

        int curActivityId = OperateActivityManager.getFirstActIdbyActType(OperateActivityConstant.ActType_DragBoat);
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
        DragonBoatManager.beginDateTime = actOpenTime5.getStartDate();
        DragonBoatManager.endDateTime = actOpenTime5.getEndDate();
        DragonBoatManager.rankRewardMap = rankRewardMap;
        DragonBoatManager.dayActivityTimeMap = dayActivityTimeMap;
        DragonBoatManager.dragonBoats = new ArrayList<>(DragonBoatManager.dragonBoatMap.values());
        /**
         * 按照指定排序规则排序
         */
        Collections.sort(DragonBoatManager.dragonBoats);
        DragonBoatManager.dayFlowMap = dayFlowMap;
        DragonBoatManager.dayActivityMap = dayActivityMap;
        DragonBoatManager.avaliableActivityIds = flowTimeMap.keySet();
        DragonBoatManager.speedUpItemId = DataManager.getCommConfig("dragonboat_ticket_speedup", -1);
        DragonBoatManager.speedDownItemId = DataManager.getCommConfig("dragonboat_ticket_slowdown", -1);
        DragonBoatManager.maxSelectTimes = DataManager.getCommConfig("dragonboat_selecttimes", 0);
        DragonBoatManager.dragonboatSpeedupRandomranges = dragonboatSpeedupRandomranges;
        DragonBoatManager.dragonboatSlowdownRandomranges = dragonboatSlowdownRandomranges;

    }

    @Override
    public void init() throws Exception {
        GmManager.reg("dragonBoat", new DragonBoatGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(DragonBoatStatusChangeEvent.class, new DragonBoatStatusChangeListenner((DragonBoatModule) module));
        eventDispatcher.reg(DragonBoatHistorySendEvent.class, new DragonBoatSendAllRankListenner((DragonBoatModule) module));
        eventDispatcher.reg(DragonBoatUpdateRankEvent.class, new DragonBoatUpdateRankListenner((DragonBoatModule) module));
    }
}
