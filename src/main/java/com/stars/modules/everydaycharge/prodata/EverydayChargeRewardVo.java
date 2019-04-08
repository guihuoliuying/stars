package com.stars.modules.everydaycharge.prodata;

public class EverydayChargeRewardVo {
	private int id;
	private int dropid;
	private int rate;
	
	public EverydayChargeRewardVo(int id, String reward) {
		String[] rewardStr = reward.split("\\+");
		this.id = id;
		setDropid(Integer.parseInt(rewardStr[0]));
		rate = Integer.parseInt(rewardStr[1]);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getDropid() {
		return dropid;
	}

	public void setDropid(int dropid) {
		this.dropid = dropid;
	}

}
