package com.stars.modules.raffle.define;

/**
 * 
 * @author likang by 2017/4/21
 * 
 */

public class RaffleReward implements RaffleDefine<Integer> {
	private int raffleId;
	private Range vipRange;
	private int rewardIndex;

	public RaffleReward() {

	}

	RaffleReward(int raffleId, Range vipRange, int rewardIndex) {
		this.raffleId = raffleId;
		this.vipRange = vipRange;
		this.rewardIndex = rewardIndex;
	}

	@Override
	public Integer getKey() {
		return raffleId;
	}

	public int getRaffleId() {
		return raffleId;
	}

	public void setRaffleId(int raffleId) {
		this.raffleId = raffleId;
	}

	public String getVipRange() {
		return vipRange.toString();
	}

	public void setVipRange(String vipRangeStr) {
		this.vipRange = Range.pase(vipRangeStr);
	}

	public int getRewardIndex() {
		return rewardIndex;
	}

	public void setRewardIndex(int rewardIndex) {
		this.rewardIndex = rewardIndex;
	}

	public int compare(int vipLevel) {
		return vipRange.compare(vipLevel);
	}

}
