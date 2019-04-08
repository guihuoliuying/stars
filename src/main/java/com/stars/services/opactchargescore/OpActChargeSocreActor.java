package com.stars.services.opactchargescore;

import com.stars.core.player.PlayerUtil;
import com.stars.db.DBUtil;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.newserverrank.NewServerRankManager;
import com.stars.modules.newserverrank.prodata.NewServerRankVo;
import com.stars.modules.opactchargescore.packet.ClientOpActChargeScore;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.OpActChargeRankPo;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;

public class OpActChargeSocreActor extends ServiceActor implements OpActChargeScore {
	private static final int MAIL_ID = 26009;
	private int curActivityId = -1;

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.OpenActChargeScore, this);
		curActivityId = OperateActivityManager.getFirstActIdbyActType(OperateActivityConstant.ActType_ChargeScore);
	}

	@Override
	public void printState() {

	}

	@Override
	public void view(long roleId) {
		List<NewServerRankVo> rankVos = NewServerRankManager.getActivityRankVoList(curActivityId);
		Set<Integer> neddRankSet = new HashSet<Integer>();
		int maxRank = 0;
		for (NewServerRankVo nsrv : rankVos) {
			int endRank = nsrv.getRankEnd();
			neddRankSet.add(endRank);
			if (endRank > maxRank) {
				maxRank = endRank;
			}
		}
		// 拿到排行榜
		int rankId = RankConstant.RANKID_CHARGESOCRE;
		List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(rankId, maxRank);
		Map<String, OpActChargeRankPo> rankMap = new HashMap<String, OpActChargeRankPo>();
		if (rankPoList != null && rankPoList.size() > 0) {
			for (AbstractRankPo rankPo : rankPoList) {
				int rank = rankPo.getRank();
				if (!neddRankSet.contains(rank)) {
					continue;
				}
				rankMap.put(String.valueOf(rank), (OpActChargeRankPo) rankPo);
			}
		}
		ClientOpActChargeScore msg = new ClientOpActChargeScore();
		msg.setRankPoMap(rankMap);
		AbstractRankPo myRankPo = ServiceHelper.rankService().getRank(rankId, roleId);
		if (myRankPo != null) {
			msg.setSelfRank((OpActChargeRankPo) myRankPo);
		}
		PlayerUtil.send(roleId, msg);
	}

	@Override
	public void closeActivity(int activityId) {
		if (activityId != curActivityId) {
			return;
		}
		sendReward();
		//清空排行榜的数据
		clearRankTable();
	}

	public void sendReward() {
		int rankType = NewServerRankManager.getRankType(curActivityId);
		if (rankType < 0) {
			LogUtil.info("OpActChargeSocreActor.sendReward get rankType fail,rankType=" + rankType);
			return;
		}
		int maxRank = NewServerRankManager.getMaxRewardRank(curActivityId);
		List<AbstractRankPo> rankPoList = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_CHARGESOCRE,
				maxRank);
		List<NewServerRankVo> activityRewadList = NewServerRankManager.getActivityRankVoList(curActivityId);
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
					AbstractRankPo rankPo = rankPoList.get(index);
					if (rankPo != null) {
						Map<Integer, Integer> rewardMap = DropUtil.executeDrop(Integer.parseInt(vo.getReward()), 1);
						ServiceHelper.emailService().sendToSingle(rankPo.getUniqueId(), MAIL_ID, 0L, "活动管理员", rewardMap,
								Integer.toString(rank));
					}
				}
			}
		}
	}

	public void clearRankTable() {
		String sqlCmd = "truncate table `opactchargerank`;";
		try {
			DBUtil.execSql(DBUtil.DB_USER, sqlCmd);
		} catch (SQLException e) {
			LogUtil.info("OpActChargeSocreActor.clearRankTable error");
		}
	}

}
