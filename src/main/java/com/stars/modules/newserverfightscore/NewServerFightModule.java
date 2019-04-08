package com.stars.modules.newserverfightscore;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.newserverfightscore.packet.ClientNSFightScore;
import com.stars.modules.newserverfightscore.prodata.NewServerFightScoreVo;
import com.stars.modules.newserverfightscore.userdata.ActRoleNsFightScore;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.ActOpenTime3;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerFightModule extends AbstractModule implements OpActivityModule {
    private ActRoleNsFightScore roleNsFightScore;

    public NewServerFightModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("新服活动-战力冲冲冲", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public int getCurShowActivityId() {
        return OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerFightScore);
    }
    
    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerFightScore);
        if (curActivityId == -1) return (byte)0;
        
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte)0;
        
        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte)0;
        
        if (labelDisappearBase instanceof NeverDisappear) {
			return (byte)1;
		}else if(labelDisappearBase instanceof DisappearByDays){
        	ActOpenTimeBase openTime = operateActVo.getActOpenTimeBase();
            if (!(openTime instanceof ActOpenTime3)) return (byte)0;
            
            ActOpenTime3 actOpenTime3 = (ActOpenTime3)openTime;
            int startDays = actOpenTime3.getStartDays();
            int openServerDays = DataManager.getServerDays();
            int continueDays = openServerDays - startDays + 1;
            int canContinueDays = ((DisappearByDays)labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte)0 : (byte)1;
        }else if (labelDisappearBase instanceof DisappearByTime) {
			Date date = ((DisappearByTime)labelDisappearBase).getDate();
			return date.getTime() < new Date().getTime() ? (byte)0 : (byte)1;
		}  
        
        return (byte)0;
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        signCalRedPoint(MConst.NewServerFightScore, RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD);
    }

    @Override
    public void onDataReq() throws Throwable {
        int curActId = getCurShowActivityId();
        if (curActId == -1)
            return;
        String sql = "select * from `actnewserverfightscore` where `roleid`=" + id()
                + " and `operateactid`=" + curActId;
        roleNsFightScore = DBUtil.queryBean(DBUtil.DB_USER, ActRoleNsFightScore.class, sql);
        if (roleNsFightScore == null) {
            roleNsFightScore = new ActRoleNsFightScore(id(), curActId);
            context().insert(roleNsFightScore);
            ServiceHelper.newServerFightScoreService().updateRewardRecord(id(), roleNsFightScore);
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD))) {
        	int curActId = getCurShowActivityId();
            if (curActId == -1){
            	redPointMap.put(RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD, null);
            	return;
            }
        	
            checkCanRewardRedPoint(curActId, redPointMap);
        }
    }

    /**
     * 请求数据
     */
    public void reqData() {
        int curActId = getCurShowActivityId();
        if (curActId == -1)
            return;
        ClientNSFightScore clientNSFightScore = new ClientNSFightScore(ClientNSFightScore.SEND_ALL_DATA);
        clientNSFightScore.setCurActId(curActId);
        clientNSFightScore.setVoMap(NewServerFightManager.getNSFSVoMap(curActId));
        clientNSFightScore.setNsfsRewardStatus(getRewardStatus(curActId));
        clientNSFightScore.setEndTime(getEndTime(curActId));
        send(clientNSFightScore);
    }

    public long getEndTime(int curActId) {
//        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActId);
//        if(operateActVo == null) return -1l;
//        ActOpenTime1 actOpenTime = (ActOpenTime1) operateActVo.getActOpenTimeBase();
//        if(actOpenTime == null) return -1l;
//        return actOpenTime.getEndDate().getTime();

        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActId);
        if (operateActVo == null) return -1l;
        ActOpenTime3 actOpenTime = (ActOpenTime3) operateActVo.getActOpenTimeBase();
        if (actOpenTime == null) return -1l;
        Date openServerDate = DataManager.getOpenServerDate();
        Date endDate = ActOpenTimeBase.getEndDateByOpenTime3(actOpenTime, openServerDate);
        return endDate.getTime();
    }

    /**
     * 领奖
     *
     * @param rewardId
     */
    public void takeReward(int rewardId) {
        int curActId = getCurShowActivityId();
        if (curActId == -1) {
            warn(I18n.get("newserver.fightscore.actFinish"));
            return;
        }
        if(roleNsFightScore==null){
            try {
                onDataReq();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        // 已领过
        if (roleNsFightScore != null && roleNsFightScore.isRewarded(rewardId)) {
            return;
        }
        NewServerFightScoreVo nsfsVo = NewServerFightManager.getNSFightScoreVo(curActId, rewardId);
        /**
         * 战力排行无法领奖，只能够活动到期发放奖励
         */
        if (nsfsVo.getType() == NewServerFightManager.REWARD_TYPE_RANKING) {
            return;
        }
        // 是否满足开始时间
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActId);
//        ActOpenTime1 actOpenTime = (ActOpenTime1) operateActVo.getActOpenTimeBase();
//        int spendDays = DateUtil.getRelativeDifferDays(actOpenTime.getStartDate(), new Date()) + 1;
        ActOpenTime3 actOpenTime = (ActOpenTime3) operateActVo.getActOpenTimeBase();
        int spendDays = 0;
        if (actOpenTime != null) {
            int openServerDays = DataManager.getServerDays();
            spendDays = ActOpenTimeBase.getOpenDaysByOpenTime3(actOpenTime, openServerDays);
        }

        if (spendDays < nsfsVo.getDay()) {
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        ToolModule toolModule = module(MConst.Tool);
        RoleModule roleModule = module(MConst.Role);
        if (roleModule.getFightScore() < nsfsVo.getMinFightScore()) {
            return;
        }
//        switch (nsfsVo.getType()) {
//            case NewServerFightManager.REWARD_TYPE_FIGHTSCORE:// 战力值类型
//                RoleModule roleModule = module(MConst.Role);
//                if (roleModule.getFightScore() < nsfsVo.getMinFightScore()) {
//                    return;
//                }
//                break;
//            case NewServerFightManager.REWARD_TYPE_RANKING:// 战力排名类型
//                // 战力排名是否达到
//                int ranking = ServiceHelper.newServerFightScoreService().canRankingReward(id(), nsfsVo.getRankLimit());
//                if (ranking == -1) {
//                    return;
//                }
//                break;
//        }
        Map<Integer, Integer> rewardMap = dropModule.executeDrop(nsfsVo.getReward(), 1, true);
        toolModule.addAndSend(rewardMap, EventType.NEWSERVERFIGHTSCORE.getCode());

        roleNsFightScore.updateRewardRecord(rewardId);
        context().update(roleNsFightScore);
        ServiceHelper.newServerFightScoreService().updateRewardRecord(id(), roleNsFightScore);
        ClientNSFightScore clientNSFightScore = new ClientNSFightScore(ClientNSFightScore.UPDATE_REWARD_RECORD);
        clientNSFightScore.setRoleNsFightScore(roleNsFightScore);
        send(clientNSFightScore);
        signCalRedPoint(MConst.NewServerFightScore, RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD);
    }

    /**
     * 可领取奖励红点
     *
     * @param curActId
     * @param redPointMap
     */
    public void checkCanRewardRedPoint(int curActId, Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        Map<Integer, Byte> statusMap = getRewardStatus(curActId);
        for (Map.Entry<Integer, Byte> entry : statusMap.entrySet()) {
            if (entry.getValue() == NewServerFightManager.REWARD_STATUS_CANREWARD) {
                if (builder.length() > 0) {
                    builder.append("+");
                }
                builder.append(entry.getKey());
            }
        }
        redPointMap.put(RedPointConst.NEW_SERVER_FIGHTSCORE_CANREWARD, builder.length() > 0 ? builder.toString() : null);
    }

    /**
     * 获得当前奖励状态
     *
     * @return 1=未到时间,2=未达条件,3=可领取,4=已领取
     */
    private Map<Integer, Byte> getRewardStatus(int curActId) {
        Map<Integer, Byte> statusMap = new HashMap<>();
        Map<Integer, NewServerFightScoreVo> voMap = NewServerFightManager.getNSFSVoMap(curActId);
        if (voMap == null)
            return statusMap;
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActId);
//        ActOpenTime1 actOpenTime = (ActOpenTime1) operateActVo.getActOpenTimeBase();
//        int spendDays = DateUtil.getRelativeDifferDays(actOpenTime.getStartDate(), new Date()) + 1;
        ActOpenTime3 actOpenTime = null;
        if (operateActVo.getActOpenTimeBase() instanceof ActOpenTime3) {
            actOpenTime = (ActOpenTime3) operateActVo.getActOpenTimeBase();
        }
        int spendDays = 0;
        if (actOpenTime != null) {
            int openServerDays = DataManager.getServerDays();
            spendDays = ActOpenTimeBase.getOpenDaysByOpenTime3(actOpenTime, openServerDays);
        }

        for (NewServerFightScoreVo vo : voMap.values()) {
            if (roleNsFightScore != null && roleNsFightScore.isRewarded(vo.getRewardId())) {
                statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_REWARDED);
                continue;
            }
            // 是否满足开始时间

            if (spendDays < vo.getDay()) {
                statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_NOTREACHTIME);
                continue;
            }
            switch (vo.getType()) {
                case NewServerFightManager.REWARD_TYPE_FIGHTSCORE:// 战力值
                    RoleModule roleModule = module(MConst.Role);
                    if (roleModule.getFightScore() >= vo.getMinFightScore()) {
                        statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_CANREWARD);
                    } else {
                        statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_NOTREACHCONDITION);
                    }
                    break;
                case NewServerFightManager.REWARD_TYPE_RANKING:// 战力排行
                    int ranking = ServiceHelper.newServerFightScoreService().canRankingReward(
                            id(), vo.getRankLimit());
                    /**
                     * 战力排行榜无法领取触发红点
                     */
                    statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_NOTREACHCONDITION);
                   /* if (ranking != -1) {
                        statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_CANREWARD);
                    } else {
                        statusMap.put(vo.getRewardId(), NewServerFightManager.REWARD_STATUS_NOTREACHCONDITION);
                    }*/
                    break;
            }
        }
        return statusMap;
    }
}
