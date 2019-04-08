package com.stars.services.opactfightscore;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.newserverrank.NewServerRankConstant;
import com.stars.modules.newserverrank.NewServerRankManager;
import com.stars.modules.newserverrank.prodata.NewServerRankVo;
import com.stars.modules.opactfightscore.packet.ClientOpActFightScore;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.opentime.*;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-20 17:20
 */
public class OpActFightScoreActor extends ServiceActor implements OpActFightScore {
    int curActivityId = -1;
    private boolean isStarting = false;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.OpActFightSocre, this);
        curActivityId = OperateActivityManager.getFirstActIdbyActType(OperateActivityConstant.ActType_NewServerFightScore0);
        checkActivityFlowState(curActivityId);
    }

    @Override
    public void printState() {

    }

    @Override
    public void view(long roleId) {
        List<NewServerRankVo> rankVos = NewServerRankManager.getActivityRankVoList(curActivityId);
        Map<String, String> dropMap = new HashMap<>();
        for (NewServerRankVo rankVo : rankVos) {
            dropMap.put(rankVo.getRankRange(), rankVo.getReward());
        }
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        String openTime = operateActVo.getOpenTime();
        String[] tmpStr = openTime.split("\\|");
        int startDay = Integer.parseInt(tmpStr[1].split("\\+")[0]);
        int endDay = Integer.parseInt(tmpStr[1].split("\\+")[1]);
        String timeDesc = operateActVo.getTimedesc();
        Date startDate = DataManager.getOpenServerDate();
        startDate.setDate(startDate.getDate() + startDay - 1);
        Date endDate = DataManager.getOpenServerDate();
        endDate.setDate(endDate.getDate() + endDay - 1);
        endDate.setHours(23);
        endDate.setMinutes(59);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startDays = simpleDateFormat.format(startDate);
        String endDays = simpleDateFormat.format(endDate);
        String timeStr = DataManager.getGametext(timeDesc);
        String time = String.format(timeStr, startDays, endDays);
        ClientOpActFightScore act = new ClientOpActFightScore();
        act.setDropMap(dropMap);
        act.setTimeDesc(time);
        PlayerUtil.send(roleId, act);
    }

    @Override
    public void checkActivityFlowState(int activityId) {
        Date now = new Date();
        int openServerDay = DataManager.getServerDays();
        List<OperateActVo> operateActVos = OperateActivityManager.getOperateActVoListByType(OperateActivityConstant.ActType_NewServerFightScore0);
        OperateActVo actVo = operateActVos.get(0);
        if (isOn(actVo.getOpen(), actVo.getActOpenTimeBase(), now, openServerDay)) {
            isStarting = true;
        } else {
            if (isStarting) {
                isStarting = false;
                sendReward(curActivityId);
            }
        }
    }

    private void sendReward(int activityId) {
        int rankType = NewServerRankManager.getRankType(activityId);
        if (rankType < 0) {
            LogUtil.info("OpActFightScoreActor.sendReward get rankType fail,rankType=" + rankType);
            return;
        }
        if (rankType == NewServerRankConstant.TYPE_FIGHT_SCORE) {
            int maxRank = NewServerRankManager.getMaxRewardRank(activityId);
            List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_FIGHTSCORE, maxRank);
            List<NewServerRankVo> activityRewadList = NewServerRankManager.getActivityRankVoList(activityId);
            if (activityRewadList != null) {
                int size = rankPoList.size();
                for (NewServerRankVo vo : activityRewadList) {
                    int rankStart = vo.getRankStart();
                    int rankEnd = vo.getRankEnd();
                    for (int rank = rankStart; rank <= rankEnd; rank++) {
                        int index = rank - 1;
                        if (index < 0 || index > size - 1) {
                            continue;
                        }
                        RoleRankPo rankPo = (RoleRankPo) rankPoList.get(index);
                        if (rankPo != null) {
                            Map<Integer, Integer> dropMap = DropUtil.executeDrop(Integer.parseInt(vo.getReward()), 1);
                            ServiceHelper.emailService().sendToSingle(rankPo.getRoleId(), NewServerRankConstant.TYPE_EMAIL_FIGHT_SCORE,
                                    0L, "系统", dropMap, String.valueOf(rankPo.getRank()));
                        }
                    }
                }
            }
        }
    }

    private boolean isOn(byte isOpen, ActOpenTimeBase openTimeBase, Date nowDate, int openServerDay) {
        if (isOpen == (byte) 0) return false;

        if (openTimeBase == null || nowDate == null) return false;

        if (openTimeBase instanceof ActOpenTime0) {//永久开放
            return true;
        } else if (openTimeBase instanceof ActOpenTime1) {//固定时间开放
            ActOpenTime1 openTime1 = (ActOpenTime1) openTimeBase;
            Date startDate = openTime1.getStartDate();
            Date endDate = openTime1.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > 0;
        } else if (openTimeBase instanceof ActOpenTime2) {//固定时间+开服前几天内开放
            ActOpenTime2 openTime2 = (ActOpenTime2) openTimeBase;
            Date startDate = openTime2.getStartDate();
            Date endDate = openTime2.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int limitDay = openTime2.getServerLimitDay();
            if (limitDay == -1) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > limitDay;
        } else if (openTimeBase instanceof ActOpenTime3) {//开服x~y天内开启
            ActOpenTime3 openTime3 = (ActOpenTime3) openTimeBase;
            int startDays = openTime3.getStartDays();
            int endDays = openTime3.getEndDays();
            if (startDays == -1 || endDays == -1) return false;
            return openServerDay >= startDays && openServerDay <= endDays;
        } else if (openTimeBase instanceof ActOpenTime6) {//开服x天内不开此活动,之后开启
            ActOpenTime6 openTime6 = (ActOpenTime6) openTimeBase;
            int days = openTime6.getDays();
            if (days == -1) return false;
            return openServerDay > days;
        }
        return false;
    }
}
