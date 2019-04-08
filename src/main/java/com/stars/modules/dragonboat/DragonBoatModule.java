package com.stars.modules.dragonboat;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.dragonboat.define.StepTime;
import com.stars.modules.dragonboat.packet.ClientDragonBoatPacket;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.modules.dragonboat.userdata.RoleBetOnDragonBoatPo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.DragonBoatRankPo;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/9.
 */
public class DragonBoatModule extends AbstractModule implements OpActivityModule {
    private static final String DRAGON_BOAT_RECORD_KEY = "DragonBoat.selectTimes";
    private OperateActVo operateActVo;
    private Map<Long, RoleBetOnDragonBoatPo> historyRecord = new HashMap<>();

    public DragonBoatModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        super.onDataReq();
        String sql = "select * from rolebetondragonboat where roleid=" + id();
        historyRecord = DBUtil.queryMap(DBUtil.DB_USER, "stagetime", RoleBetOnDragonBoatPo.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        super.onInit(isCreation);
        int curActivityId = getCurShowActivityId();
        if (curActivityId != -1) {
            int selectTimes = getInt(DRAGON_BOAT_RECORD_KEY, -1);
            if (selectTimes == -1) {
                selectTimes = 0;
                setInt(DRAGON_BOAT_RECORD_KEY, selectTimes);
            }
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        setInt(DRAGON_BOAT_RECORD_KEY, 0);
        /**
         * 更新活动提示
         */
        ServiceHelper.opDragonBoatService().updateStepTime();
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_DragBoat);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            }

        }
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    /**
     * 下发活动数据
     */
    public void sendActivityData(boolean isNotify) {
        ClientDragonBoatPacket clientDragonBoatPacket = new ClientDragonBoatPacket(ClientDragonBoatPacket.SEND_ACTIVITY_DATA);
        StepTime stepTime = ServiceHelper.opDragonBoatService().nextStep();
        clientDragonBoatPacket.setStepTime(stepTime);
        clientDragonBoatPacket.setMaxSelectTimes(DragonBoatManager.maxSelectTimes);
        clientDragonBoatPacket.setMySelectTimes(getInt(DRAGON_BOAT_RECORD_KEY));
        clientDragonBoatPacket.setIsNotify(isNotify ? 1 : 0);
        /**
         * 下一步的阶段
         */
        switch (stepTime.getStep()) {
            case DragonBoatConst.BET_ON: {
                clientDragonBoatPacket.setMaxSelectTimes(DragonBoatManager.maxSelectTimes);
                clientDragonBoatPacket.setMySelectTimes(getInt(DRAGON_BOAT_RECORD_KEY));
                Integer betOnDragonBoatId = ServiceHelper.opDragonBoatService().getBetOnDragonBoatId(id());
                if (betOnDragonBoatId != null) {
                    clientDragonBoatPacket.setBetOnedDragonBoatId(betOnDragonBoatId);
                }
            }
            break;
            case DragonBoatConst.RACING: {
                Integer betOnDragonBoatId = ServiceHelper.opDragonBoatService().getBetOnDragonBoatId(id());
                if (betOnDragonBoatId != null) {
                    clientDragonBoatPacket.setBetOnedDragonBoatId(betOnDragonBoatId);
                    DragonBoatVo dragonBoat = DragonBoatManager.dragonBoatMap.get(betOnDragonBoatId);
                    clientDragonBoatPacket.setBetOnedDragonBoatName(dragonBoat.getName());
                }

            }
            break;
            case DragonBoatConst.WAITING:
            case DragonBoatConst.SHOW: {
                Integer betOnDragonBoatId = ServiceHelper.opDragonBoatService().getBetOnDragonBoatId(id());
                if (betOnDragonBoatId != null) {
                    clientDragonBoatPacket.setBetOnedDragonBoatId(betOnDragonBoatId);
                    clientDragonBoatPacket.setBetOnedDragonBoatName(DragonBoatManager.dragonBoatMap.get(betOnDragonBoatId).getName());
                    Long activityKey = ServiceHelper.opDragonBoatService().getActivityKey();
                    DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_DRAGON_BOAT, betOnDragonBoatId, activityKey);
                    clientDragonBoatPacket.setBetOnedDragonBoatSpeed(dragonBoatRankPo.getSpeed());
                }
            }
            break;
            case DragonBoatConst.FINISH: {
                Integer betOnDragonBoatId = ServiceHelper.opDragonBoatService().getBetOnDragonBoatId(id());
                if (betOnDragonBoatId != null) {
                    clientDragonBoatPacket.setBetOnedDragonBoatId(betOnDragonBoatId);
                    Long activityKey = ServiceHelper.opDragonBoatService().getActivityKey();
                    DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_DRAGON_BOAT, betOnDragonBoatId, activityKey);
                    clientDragonBoatPacket.setBetOnedDragonBoatRank(dragonBoatRankPo.getRank());
                }
            }
            break;
        }
        send(clientDragonBoatPacket);
    }

    /**
     * 下注
     *
     * @param dragonBoatId
     */
    public void betOn(Integer dragonBoatId) {
        int selectTimes = getInt(DRAGON_BOAT_RECORD_KEY, -1);
        if (selectTimes <= DragonBoatManager.maxSelectTimes) {
            StepTime nextStep = ServiceHelper.opDragonBoatService().nextStep();
            if (nextStep.getStep() - 1 != DragonBoatConst.BET_ON) {
                warn(DataManager.getGametext("dragonboat_select_overtime"));
                return;
            }
            setInt(DRAGON_BOAT_RECORD_KEY, selectTimes + 1);
            Long stageTime = ServiceHelper.opDragonBoatService().getActivityKey();
            if (!historyRecord.containsKey(stageTime)) {
                RoleBetOnDragonBoatPo roleBetOnDragonBoat = new RoleBetOnDragonBoatPo(id(), stageTime, dragonBoatId);
                context().insert(roleBetOnDragonBoat);
                historyRecord.put(stageTime, roleBetOnDragonBoat);
            }
            ServiceHelper.opDragonBoatService().betOn(id(), dragonBoatId);

            ClientDragonBoatPacket clientDragonBoatPacket = new ClientDragonBoatPacket(ClientDragonBoatPacket.SEND_BET_ON_TIPS);
            clientDragonBoatPacket.setBetOnedDragonBoatId(dragonBoatId);
            send(clientDragonBoatPacket);
        } else {
            warn(DataManager.getGametext("dragonboat_select_timesshort"));
        }
    }

    /**
     * 给龙舟投票
     *
     * @param dragonBoatId
     */
    public void vote(Integer dragonBoatId) {
        ToolModule toolModule = module(MConst.Tool);
        Integer betOnDragonBoatId = ServiceHelper.opDragonBoatService().getBetOnDragonBoatId(id());
        if (betOnDragonBoatId == null) {
            warn("未押注禁止投票");
            LogUtil.error("未押注禁止投票");
            return;
        }
        StepTime nextStep = ServiceHelper.opDragonBoatService().nextStep();
        if (nextStep.getStep() - 1 != DragonBoatConst.RACING) {
            warn("");
            return;
        }
        boolean success = false;
        if (dragonBoatId.equals(betOnDragonBoatId)) {
            success = toolModule.contains(DragonBoatManager.speedUpItemId, 1);
            toolModule.useToolByItemId(DragonBoatManager.speedUpItemId, 1, dragonBoatId);
        } else {
            success = toolModule.contains(DragonBoatManager.speedDownItemId, 1);
            toolModule.useToolByItemId(DragonBoatManager.speedDownItemId, 1, dragonBoatId);
        }
        // TODO: 2017/5/10  
        if (success) {

        } else {
            warn("物品不足，无法投票");
        }

    }

    /**
     * 更新龙舟速度
     *
     * @param dragonBoatId
     * @param speed
     */
    public void updateDragonBoatSpeed(Integer dragonBoatId, int speed) {
        ServiceHelper.opDragonBoatService().updateSpeed(id(), dragonBoatId, speed);
        updateOneRank();
    }


    /**
     * 下发指定回合的排行榜
     *
     * @param
     */
    public void sendAllRank() {
        ServiceHelper.rankService().sendRankList4DragonBoat(RankConstant.RANKID_DRAGON_BOAT, id());
    }

    /**
     * 更新排行榜
     */
    public void updateOneRank() {
        ClientDragonBoatPacket clientDragonBoatPacket = new ClientDragonBoatPacket(ClientDragonBoatPacket.UPDATE_ONE_RANK);
        send(clientDragonBoatPacket);
    }

    /**
     * 下发所有历史排行榜数据
     *
     * @param rankMap
     */
    public void sendAllHistoryRank(Map<Long, List<AbstractRankPo>> rankMap) {
        ClientDragonBoatPacket clientDragonBoatPacket = new ClientDragonBoatPacket(ClientDragonBoatPacket.SEND_RANK);
        clientDragonBoatPacket.setRankPoMap(rankMap);
        clientDragonBoatPacket.setHistoryRecord(historyRecord);
        send(clientDragonBoatPacket);
    }

    /**
     * 下发奖励预览
     */
    public void sendRewardPreview() {
        ClientDragonBoatPacket clientDragonBoatPacket = new ClientDragonBoatPacket(ClientDragonBoatPacket.SEND_REWARD_PREVIEW);
        send(clientDragonBoatPacket);
    }
}
