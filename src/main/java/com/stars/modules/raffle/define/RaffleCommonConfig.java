package com.stars.modules.raffle.define;

import com.stars.modules.data.DataManager;
import com.stars.util.StringUtil;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class RaffleCommonConfig {

	public final RaffleCost cost;
	public final int times;// 每轮抽奖次数
	public final int dailyTimes;// 每日的抽奖次数
	public final int originMoney;
	public final int raffleCostTenTimes; //10次抽奖需要消耗次数

	RaffleCommonConfig(RaffleCost cost, int times, int dailyTimes, int originMoney, int raffleCostTenTimes) {
		this.cost = cost;
		this.times = times;
		this.dailyTimes = dailyTimes;
		this.originMoney = originMoney;
		this.raffleCostTenTimes = raffleCostTenTimes;
	}

	public static RaffleCommonConfig createConfig() {
		String costContent = DataManager.getCommConfig("raffle_cost");
		RaffleCost cost = RaffleCost.parse(costContent);
		int times = DataManager.getCommConfig("raffle_maxnum", 0);
		int dailyTimes = DataManager.getCommConfig("raffle_dailynum", 0);
		int originMoney = DataManager.getCommConfig("raffle_originmoney", 0);
		int raffeCostTenTimes = DataManager.getCommConfig("raffle_cost_tentimes",10);
		return new RaffleCommonConfig(cost, times, dailyTimes, originMoney,raffeCostTenTimes);
	}

	public static class RaffleCost {
		public final int itemId;
		public final int num;

		public RaffleCost(int itemId, int num) {
			this.itemId = itemId;
			this.num = num;
		}

		public static RaffleCost parse(String costContent) {
			if (StringUtil.isEmpty(costContent)) {
				throw new RuntimeException("<RaffleCommonConfig.RaffleCost> parse() costContent is null!");
			}
			String[] params = costContent.split("\\+");
			if (params.length != 2) {
				throw new RuntimeException("<RaffleCommonConfig.RaffleCost> parse() params is error!");
			}
			int itemId = Integer.parseInt(params[0]);
			int num = Integer.parseInt(params[1]);
			return new RaffleCost(itemId, num);
		}

	}
}
