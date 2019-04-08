package com.stars.modules.raffle.define;

import com.stars.util.LogUtil;

import java.util.*;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class RaffleDefineManager {

	public static final RaffleDefineManager instance = new RaffleDefineManager();

	private List<Integer> rewardKeys;
	private Map<Integer, RaffleReward> raffleRewards;
	private Map<Integer, List<RaffleRewardEntry>> raffleRewardEntrys;
	private List<SumRewardRate> rewardRates;
	private RaffleCommonConfig commonConfig;

	/**
	 * 供配置热更模块调用
	 */

	public void reload() {
		reloadReward();
		reloadRewardEntry();
		reloadRewardRates();
		reloadCommonConfig();
	}

	private void reloadReward() {
		Map<Integer, RaffleReward> temp = new HashMap<Integer, RaffleReward>();
		List<Integer> keys = new ArrayList<Integer>();
		List<RaffleReward> rewards = RaffleRewardDao.instance.selectAllRewards();
		for (RaffleReward reward : rewards) {
			keys.add(reward.getKey());
			temp.put(reward.getKey(), reward);
		}
		Collections.sort(keys);
		this.rewardKeys = keys;
		this.raffleRewards = temp;
	}

	private void reloadRewardEntry() {
		List<RaffleRewardEntry> rewardEntrys = RaffleRewardDao.instance.selectAllRewardEntrys();
		Map<Integer, List<RaffleRewardEntry>> temp = new HashMap<Integer, List<RaffleRewardEntry>>();
		for (RaffleRewardEntry entry : rewardEntrys) {
			int index = entry.getRaffleIndexId();
			List<RaffleRewardEntry> list = temp.get(index);
			if (list == null) {
				list = new ArrayList<RaffleRewardEntry>();
				temp.put(index, list);
			}
			list.add(entry);
		}
		this.raffleRewardEntrys = temp;
	}

	private void reloadRewardRates() {
		List<SumRewardRate> temp = new ArrayList<SumRewardRate>();
		temp.addAll(RaffleRewardDao.instance.selectAllSumRewardRate());
		Collections.sort(temp);
		this.rewardRates = temp;
	}

	private void reloadCommonConfig() {
		this.commonConfig = RaffleCommonConfig.createConfig();
	}

	/**
	 * 获取每个奖励组的所有条目
	 * 
	 * @param rewardIndex
	 * @return
	 */

	public List<RaffleRewardEntry> getRewardEntrysBy(int rewardIndex) {
		if (raffleRewardEntrys == null || !raffleRewardEntrys.containsKey(rewardIndex)) {
			return new ArrayList<RaffleRewardEntry>(0);
		}
		return raffleRewardEntrys.get(rewardIndex);
	}

	public int getRewardEntrysSize(int rewardIndex) {
		if (raffleRewardEntrys == null || !raffleRewardEntrys.containsKey(rewardIndex)) {
			return 0;
		}
		return raffleRewardEntrys.get(rewardIndex).size();
	}

	public RaffleRewardEntry getRewardEntry(int rewardIndex, int position) {
		if (raffleRewardEntrys == null || !raffleRewardEntrys.containsKey(rewardIndex)) {
			return null;
		}
		List<RaffleRewardEntry> list = raffleRewardEntrys.get(rewardIndex);
		if (position < 0 || position >= list.size()) {
			com.stars.util.LogUtil.error(String.format("RaffleDefineManager getRewardEntry()  position is outOff range! position=%s",
					position));
			return null;
		}
		
		return list.get(position);
	}

	/***
	 * 
	 * @param vipLevel
	 * @return
	 */

	public RaffleReward getRaffleRewardBy(int vipLevel) {
		if (rewardKeys == null || raffleRewards == null) {
			return null;
		}
		int low = 0, high = rewardKeys.size() - 1;
		int mid = 0;
		while (low <= high) {
			mid = (low + high) / 2;
			int id = rewardKeys.get(mid);
			RaffleReward reward = raffleRewards.get(id);
			int result = reward.compare(vipLevel);
			if (result == 0) {
				break;
			}
			if (result > 0) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}
		// 查找失败
		if (low > high) {
			mid = low;
			LogUtil.error(String.format("RaffleDefineManager.getRaffleRewardBy() could not find vip range! vipLevel=%s",
					vipLevel));
		}
		int id = rewardKeys.get(mid);
		return raffleRewards.get(id);
	}

	public List<SumRewardRate> getRewardRates() {
		return rewardRates;
	}

	public SumRewardRate getRateBy(int index) {
		if (rewardRates == null || rewardRates.size() <= 0) {
			return null;
		}
		if (index < 0 || index > rewardRates.size() - 1) {
			return null;
		}
		return rewardRates.get(index);
	}

	public RaffleCommonConfig getCommonConfig() {
		return commonConfig;
	}

}
