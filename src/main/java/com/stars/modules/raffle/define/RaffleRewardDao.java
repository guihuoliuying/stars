package com.stars.modules.raffle.define;

import com.stars.core.db.DBUtil;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class RaffleRewardDao {
	private static final String SELECT_REWARD = "select * from rafflereward;";
	private static final String SELECT_REWARD_ENTRY = "select * from rafflerewardindex;";
	private static final String SELECT_SUM_REWARD_RATE = "select * from sumrewardrate;";

	public static final RaffleRewardDao instance = new RaffleRewardDao();

	public List<RaffleReward> selectAllRewards() {
		List<RaffleReward> rewards = new ArrayList<RaffleReward>();
		try {
			List<RaffleReward> list = DBUtil.queryList(DBUtil.DB_PRODUCT, RaffleReward.class, SELECT_REWARD);
			rewards.addAll(list);
		} catch (SQLException e) {
			com.stars.util.LogUtil.error("<RaffleRewardDao>selectAllReward() error!", e);
		}
		return rewards;
	}

	public List<RaffleRewardEntry> selectAllRewardEntrys() {
		List<RaffleRewardEntry> rewardEntrys = new ArrayList<RaffleRewardEntry>();
		try {
			List<RaffleRewardEntry> list = DBUtil.queryList(DBUtil.DB_PRODUCT, RaffleRewardEntry.class,
					SELECT_REWARD_ENTRY);
			rewardEntrys.addAll(list);
		} catch (SQLException e) {
			com.stars.util.LogUtil.error("<RaffleRewardDao>selectAllReward() error!", e);
		}
		return rewardEntrys;
	}

	public List<SumRewardRate> selectAllSumRewardRate() {
		List<SumRewardRate> rewardRates = new ArrayList<SumRewardRate>();
		try {
			List<SumRewardRate> list = DBUtil.queryList(DBUtil.DB_PRODUCT, SumRewardRate.class, SELECT_SUM_REWARD_RATE);
			rewardRates.addAll(list);
		} catch (SQLException e) {
			LogUtil.error("<RaffleRewardDao>selectAllReward() error!", e);
		}
		return rewardRates;
	}

}
