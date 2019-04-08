package com.stars.services.bestcp;

import com.stars.modules.bestcp520.BestCPManager;
import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.BestCPRankPo;
import com.stars.services.rank.userdata.BestCPVoterRankPo;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class OpBestCPServiceActor extends ServiceActor implements OpBestCPService {
    int curActId;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.OpBestCpService, this);
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_BestCP520);
        if (curActId == -1)
            return;
        openActivity(curActId);
    }

    @Override
    public void printState() {

    }

    @Override
    public void openActivity(int activityId) {
        curActId = activityId;
        List<AbstractRankPo> frontRank = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_BEST_CP, 10);
        if (frontRank.size() == 0) {
            for (BestCP bestCP : BestCPManager.bestCPMap.values()) {
                BestCPRankPo bestCPRankPo = new BestCPRankPo(bestCP.getCpId(), 0);
                bestCPRankPo.setRankId(RankConstant.RANKID_BEST_CP);
                ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_BEST_CP,
                        bestCPRankPo);
            }
        }

    }

    @Override
    public void closeActivity(int activityId) {
        if (curActId != activityId)
            return;
        curActId = -1;
        for (BestCP bestCP : BestCPManager.bestCPMap.values()) {
            int cpId = bestCP.getCpId();
            List<AbstractRankPo> frontRanks = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_BEST_CP_VOTER, BestCPManager.bestCPRankDisplayMap.get(cpId), cpId);
            for (AbstractRankPo rankPo : frontRanks) {
                BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) rankPo;
                long roleId = bestCPVoterRankPo.getRoleId();
                Integer dropId = BestCPManager.rankRewardMap.get(cpId).get(rankPo.getRank());
                Map<Integer, Integer> affix = DropUtil.executeDrop(dropId, 1);
                ServiceHelper.emailService().sendToSingle(roleId, 26010, 0L, "系统", affix, bestCPVoterRankPo.getBestCP().getCpName(), bestCPVoterRankPo.getRank() + "");
            }
        }
    }

    @Override
    public void dailyReset() {

    }

    @Override
    public void vote(long roleid, int cpId) {
        /**
         *更新最佳组合排行榜
         */
        BestCPRankPo bestCPRankPo = (BestCPRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_BEST_CP, cpId);
        BestCPRankPo newBestCpRankPo = (BestCPRankPo) bestCPRankPo.copy();
        newBestCpRankPo.setVoteSum(newBestCpRankPo.getVoteSum() + 1);
        ServiceHelper.rankService().updateRank(RankConstant.RANK_TYPE_BEST_CP, newBestCpRankPo);
        /**
         *更新最佳组合角色投票排行榜
         */
        BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) ServiceHelper.rankService().getRank(RankConstant.RANKID_BEST_CP_VOTER, roleid, cpId);
        BestCPVoterRankPo newBestCpVoterRankPo;
        if (bestCPVoterRankPo != null) {
            newBestCpVoterRankPo = (BestCPVoterRankPo) bestCPVoterRankPo.copy();
            newBestCpVoterRankPo.setVoteSum(newBestCpVoterRankPo.getVoteSum() + 1);
        } else {
            newBestCpVoterRankPo = new BestCPVoterRankPo(roleid, cpId, 1);
            newBestCpVoterRankPo.setRankId(RankConstant.RANKID_BEST_CP_VOTER);
        }
        ServiceHelper.rankService().updateRank4BestCP(RankConstant.RANK_TYPE_BEST_CP_VOTER, newBestCpVoterRankPo, cpId);
    }
}
