package com.stars.services.dragonboat;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.activityflow.ActivityFlowUtil;
import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.dragonboat.DragonBoatConst;
import com.stars.modules.dragonboat.DragonBoatManager;
import com.stars.modules.dragonboat.define.StepTime;
import com.stars.modules.dragonboat.event.DragonBoatStatusChangeEvent;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.modules.dragonboat.userdata.RoleBetOnDragonBoatPo;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.rank.RankManager;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.chat.ChatManager;
import com.stars.services.dragonboat.flow.DragonBoatFlow;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.DragonBoatRankPo;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class OpDragonBoatServiceActor extends ServiceActor implements OpDragonBoatService {
    private StepTime nextStepTime;
    private DragonBoatFlow dragonBoatFlow = new DragonBoatFlow();
    private Long activityKey;
    /**
     * stageTime, <roleid,dragonboatid>
     */

    private Map<Long, Map<Long, Integer>> historyBetOnMap;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.OpDragonBoatService, this);
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_DragBoat);
        if (curActId == -1)
            return;
        openActivity(curActId);
    }


    @Override
    public void openActivity(Integer activityId) {
        try {
            dragonBoatFlow.init(SchedulerHelper.getScheduler(), DragonBoatManager.dayFlowMap);
        } catch (Exception e) {
            LogUtil.error("龙舟日定时器初始化失败", e);
        }
        try {
            String sql = "select * from rolebetondragonboat ";
            List<RoleBetOnDragonBoatPo> historyRecord = DBUtil.queryList(DBUtil.DB_USER, RoleBetOnDragonBoatPo.class, sql);
            historyBetOnMap = new ConcurrentHashMap<>();
            for (RoleBetOnDragonBoatPo roleBetOnDragonBoat : historyRecord) {
                if (!historyBetOnMap.containsKey(roleBetOnDragonBoat.getStageTime())) {
                    Map<Long, Integer> betOnMap = new HashMap<>();
                    historyBetOnMap.put(roleBetOnDragonBoat.getStageTime(), betOnMap);
                }
                Map<Long, Integer> betOnTmp = historyBetOnMap.get(roleBetOnDragonBoat.getStageTime());
                betOnTmp.put(roleBetOnDragonBoat.getRoleId(), roleBetOnDragonBoat.getDragonBoatId());
            }
        } catch (SQLException e) {
            LogUtil.error("初始化赛龙舟历史投票记录失败", e);
        }
    }

    @Override
    public void startBetOn() {
        LogUtil.info("进入开始押注阶段");
        ServiceHelper.roleService().noticeAll(new DragonBoatStatusChangeEvent(nextStepTime.getStep() - 1, getActivityKey()));
    }

    @Override
    public void betOn(long roleId, int dragonBoatId) {
        LogUtil.info("下注：" + roleId);
        if (nextStepTime.getStep() - 1 != DragonBoatConst.BET_ON) {
            return;
        }
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        Map<Long, Integer> betOnMap = historyBetOnMap.get(getActivityKey());
        if (betOnMap == null) {
            betOnMap = new HashMap<Long, Integer>();
            historyBetOnMap.put(getActivityKey(), betOnMap);
        }
        if (!betOnMap.containsKey(roleId)) {
            betOnMap.put(roleId, dragonBoatId);
        } else {
            LogUtil.error("同一回合禁止多次下注");
        }
    }

    @Override
    public void startRacing() {
        LogUtil.info("进入开始竞速阶段");
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        ServiceHelper.roleService().noticeAll(new DragonBoatStatusChangeEvent(nextStepTime.getStep() - 1, getActivityKey()));
        String dragonboatTipsBegin = DataManager.getGametext("dragonboat_tips_begin");
        for (DragonBoatVo dragonBoat : DragonBoatManager.dragonBoats) {
            DragonBoatRankPo rank = new DragonBoatRankPo(getActivityKey(), dragonBoat.getDragonBoatId(), 0, 0, 0);
            ServiceHelper.rankService().updateRank4DragonBoat(RankConstant.RANK_TYPE_DRAGON_BOAT, rank, getActivityKey());
        }
        ServiceHelper.chatService().announce(dragonboatTipsBegin);
        ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_WORLD, 0L, 0L, dragonboatTipsBegin, true);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.DragonBoatVote, new DragonFakeVote(), 5, 10, TimeUnit.SECONDS);
    }

    @Override
    public void updateSpeed(long roleId, int dragonBoatId, int changedSpeed) {
        if (nextStepTime.getStep() - 1 != DragonBoatConst.RACING) {
            return;
        }
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        DragonBoatRankPo rank = (DragonBoatRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_DRAGON_BOAT, dragonBoatId, getActivityKey());
        if (rank == null) {
            rank = new DragonBoatRankPo(getActivityKey(), dragonBoatId, 0, 0, 0);
        }
        DragonBoatRankPo newRank = (DragonBoatRankPo) rank.copy();
        ItemVo upItemVo = ToolManager.getItemVo(DragonBoatManager.speedUpItemId);
        ItemVo downItemVo = ToolManager.getItemVo(DragonBoatManager.speedDownItemId);
        String upUnitStr = upItemVo.getFunction().split("\\|")[1];
        String downUnitStr = downItemVo.getFunction().split("\\|")[1];
        int upUnit = Integer.parseInt(upUnitStr);
        int downUnit = Integer.parseInt(downUnitStr);
        if (changedSpeed > 0) {
            newRank.setUpNum(rank.getUpNum() + 1);
        } else {
            newRank.setDownNum(rank.getDownNum() + 1);
        }
        int speed = newRank.getUpNum() * upUnit + newRank.getDownNum() * downUnit;
        newRank.setSpeed(speed > 0 ? speed : 0);
        ServiceHelper.rankService().updateRank4DragonBoat(RankConstant.RANK_TYPE_DRAGON_BOAT, newRank, getActivityKey());
    }

    @Override
    public void finishRound() {
        LogUtil.info("进入开始结束阶段{}", getActivityKey());
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        List<AbstractRankPo> frontRank = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_DRAGON_BOAT, 100, getActivityKey());
        Map<Integer, DragonBoatRankPo> dragonBoatRankPoMap = new HashMap<>();
        for (AbstractRankPo abstractRankPo : frontRank) {
            DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) abstractRankPo;
            dragonBoatRankPoMap.put(dragonBoatRankPo.getDragonBoatId(), dragonBoatRankPo);
        }
        List<RankAwardVo> rankAward = RankManager.getRankAward(DragonBoatConst.rankAwardId);
        int email = rankAward.get(0).getEmail();
        Map<Long, Integer> betOnMap = historyBetOnMap.get(getActivityKey());
        if (betOnMap != null) {
            for (Map.Entry<Long, Integer> entry : betOnMap.entrySet()) {
                Long roleId = entry.getKey();
                Integer betOnDragonBoatId = entry.getValue();
                DragonBoatRankPo dragonBoatRankPo = dragonBoatRankPoMap.get(betOnDragonBoatId);
                int rank = dragonBoatRankPo.getRank();
                String reward = DragonBoatManager.rankRewardMap.get(rank);
                Map<Integer, Integer> rewardMap = StringUtil.toMap(reward, Integer.class, Integer.class, '=', '|');
                ServiceHelper.emailService().sendToSingle(roleId, email, 0L, "系统", rewardMap, rank + "");
            }
        }
        LogUtil.info("龙舟回合结束，通知客户端刷新");
        ServiceHelper.roleService().noticeAll(new DragonBoatStatusChangeEvent(nextStepTime.getStep() - 1, getActivityKey()));
    }

    @Override
    public void startWaiting() {
        LogUtil.info("进入开始等待阶段");
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        ServiceHelper.roleService().noticeAll(new DragonBoatStatusChangeEvent(nextStepTime.getStep() - 1, getActivityKey()));
        SchedulerManager.shutDownNow(ExcutorKey.DragonBoatVote);
        ServiceHelper.opDragonBoatService().updateSpeedFake();
    }

    @Override
    public void startShow() {
        LogUtil.info("进入开始展示阶段");
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return;
        }
        ServiceHelper.roleService().noticeAll(new DragonBoatStatusChangeEvent(nextStepTime.getStep() - 1, getActivityKey()));
    }

    /**
     * 获取角色下注的龙舟
     *
     * @param roleId
     * @return
     */
    @Override
    public Integer getBetOnDragonBoatId(Long roleId) {
        if (getActivityKey() == null) {
            LogUtil.error("无活动句柄：禁止后续操作");
            return null;
        }
        Map<Long, Integer> betOnMap = historyBetOnMap.get(getActivityKey());
        if (betOnMap == null) {
            return null;
        }
        return betOnMap.get(roleId);
    }


    @Override
    public Long getActivityKey() {
        return activityKey;
    }

    @Override
    public void closeActivity(int activityId) {
        try {
            dragonBoatFlow.stop(SchedulerHelper.getScheduler());
        } catch (Exception e) {
            LogUtil.error("赛龙舟活动结束，清理定时器失败", e);
        }
    }

    @Override
    public void updateSpeedFake() {
        List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_DRAGON_BOAT, 10, getActivityKey());
        int lastSpeed = 0;
        for (int index = rankPoList.size() - 1; index >= 0; index--) {
            DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) rankPoList.get(index);
            DragonBoatRankPo newRank = null;
            if (lastSpeed >= dragonBoatRankPo.getSpeed()) {
                newRank = (DragonBoatRankPo) dragonBoatRankPo.copy();
                int speed= lastSpeed + 1;
                newRank.setSpeed(speed);
                newRank.setUpNum(newRank.getDownNum() + speed); //增益票也符合规则
                ServiceHelper.rankService().updateRank4DragonBoat(RankConstant.RANK_TYPE_DRAGON_BOAT, newRank, getActivityKey());
            }
            if (newRank != null) {
                lastSpeed = newRank.getSpeed();
            } else {
                lastSpeed = dragonBoatRankPo.getSpeed();
            }
        }

    }

    @Override
    public void updateVoteFake() {
        List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_DRAGON_BOAT, 10, getActivityKey());
        for (int index = rankPoList.size() - 1; index >= 0; index--) {
            DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) rankPoList.get(index);
            int down = RandomUtil.rand(DragonBoatManager.dragonboatSlowdownRandomranges[0], DragonBoatManager.dragonboatSlowdownRandomranges[1]);
            int up = RandomUtil.rand(DragonBoatManager.dragonboatSpeedupRandomranges[0], DragonBoatManager.dragonboatSpeedupRandomranges[1]);
            ItemVo upItemVo = ToolManager.getItemVo(DragonBoatManager.speedUpItemId);
            ItemVo downItemVo = ToolManager.getItemVo(DragonBoatManager.speedDownItemId);
            String upUnitStr = upItemVo.getFunction().split("\\|")[1];
            String downUnitStr = downItemVo.getFunction().split("\\|")[1];
            int upUnit = Integer.parseInt(upUnitStr);
            int downUnit = Integer.parseInt(downUnitStr);
            DragonBoatRankPo newRank = (DragonBoatRankPo) dragonBoatRankPo.copy();
            newRank.setUpNum(dragonBoatRankPo.getUpNum() + up);
            newRank.setDownNum(dragonBoatRankPo.getDownNum() + down);
            int speed = newRank.getUpNum() * upUnit + newRank.getDownNum() * downUnit;
            newRank.setSpeed(speed > 0 ? speed : 0);
            ServiceHelper.rankService().updateRank4DragonBoat(RankConstant.RANK_TYPE_DRAGON_BOAT, newRank, getActivityKey());
        }
    }


    @Override
    public void printState() {

    }

    /**
     * 更新当前时间的活动状态
     */
    @Override
    public void updateStepTime() {
        nextStepTime = null;
        long now = DateUtil.getSecondTime();
        Map.Entry<Integer, String> firstEntry = null;
        int index = 0;
        long lastTime = 0;
        for (Map.Entry<Integer, String> entry : DragonBoatManager.dayFlowMap.entrySet()) {
            if (index == 0) {
                firstEntry = entry;
            }
            index++;
            long timeInMillisByCronExpr = ActivityFlowUtil.getTimeInMillisByCronExpr(entry.getValue());
            /**
             * 比较秒即可，
             */
            if (now < timeInMillisByCronExpr / 1000) {
                int currentStep = (entry.getKey() - 1) % 5 + 1;
                int time = (entry.getKey() - 1) / 5 + 1;
                Integer activityId = DragonBoatManager.dayActivityMap.get(time);
                Map<Integer, String> activityFlowConfig = DataManager.getActivityFlowConfig(activityId);
                /**
                 * 获取第四步的表达式作为本轮活动的时间
                 */
                String cron = activityFlowConfig.get(4);
                if (currentStep != 1) {//下一阶段重新开始不要清除活动句柄
                    activityKey = (ActivityFlowUtil.getTimeInMillisByCronExpr(cron) / 1000) * 1000;
                }
                nextStepTime = new StepTime(StepTime.IS_TODAY, timeInMillisByCronExpr, currentStep, lastTime);
                break;
            }
            lastTime = timeInMillisByCronExpr;
        }
        if (nextStepTime == null) {
            int differDays = DateUtil.getRelativeDifferDays(new Date(), DragonBoatManager.endDateTime);
            if (differDays > 0) {
                int currentStep = (firstEntry.getKey() - 1) % 5 + 1;
                long timeInMillisByCronExpr = ActivityFlowUtil.getTimeInMillisByCronExpr(firstEntry.getValue());
                nextStepTime = new StepTime(StepTime.IS_TOMORROW, timeInMillisByCronExpr, currentStep, lastTime);
            } else {
                nextStepTime = new StepTime(StepTime.IS_END, -1L, 1, lastTime);
            }
        }
    }

    /**
     * 获取下一阶段的状态
     *
     * @return
     */
    @Override
    public StepTime nextStep() {
        if (nextStepTime == null) {
            updateStepTime();
        }
        return nextStepTime;
    }

    class DragonFakeVote implements Runnable {

        @Override
        public void run() {
            ServiceHelper.opDragonBoatService().updateVoteFake();
        }
    }
}
