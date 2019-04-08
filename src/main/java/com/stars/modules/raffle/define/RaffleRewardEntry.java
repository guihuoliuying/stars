package com.stars.modules.raffle.define;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.List;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class RaffleRewardEntry implements RaffleDefine<String> {
	private int raffleIndexId;
	private int position;
	private List<Integer> itemReward;
	private Range randomMoney;

	public RaffleRewardEntry() {

	}

	RaffleRewardEntry(int raffleIndexId, int position, List<Integer> itemReward, Range randomMoney) {
		this.raffleIndexId = raffleIndexId;
		this.position = position;
		this.itemReward = itemReward;
		this.randomMoney = randomMoney;
	}

	@Override
	public String getKey() {
		return String.format("%s_%s", raffleIndexId, position);
	}

	public int getRaffleIndexId() {
		return raffleIndexId;
	}

	public void setRaffleIndexId(int raffleIndexId) {
		this.raffleIndexId = raffleIndexId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<Integer> getItemRewardList() {
		return itemReward;
	}

	public String getItemReward() {
		return StringUtil.makeString(itemReward, '+');
	}

	public void setItemReward(String itemReward) {
		try {
			this.itemReward = StringUtil.toArrayList(itemReward, Integer.class, '+');
		} catch (Exception e) {
			LogUtil.error("RaffleRewardEntry parse error!", e);
		}
	}

	public String getRandomMoney() {
		return randomMoney.toString();
	}

	public Range getMoneyRange() {
		return randomMoney;
	}

	public void setRandomMoney(String randomMoney) {
		this.randomMoney = Range.pase(randomMoney);
	}

}
