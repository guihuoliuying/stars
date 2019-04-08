package com.stars.modules.everydaycharge.prodata;

import java.util.HashMap;
import java.util.Map;

public class EverydayChargeVo {
	private int everydayid;
	private String levelrange;
	private int minLv;
	private int maxLv;
	private String viplevelrange;
	private int minVipLv;
	private int maxVipLv;
	private String reward;
	private Map<Integer, EverydayChargeRewardVo> rewardMap;
	private int operateactid;

	public String getLevelrange() {
		return levelrange;
	}
	
	public void setLevelrange(String levelrange) {
		this.levelrange = levelrange;
		String[] lv = levelrange.split("\\+");
		minLv = Integer.parseInt(lv[0]);
		maxLv = Integer.parseInt(lv[1]);
	}

	public int getMinLv() {
		return minLv;
	}

	public void setMinLv(int minLv) {
		this.minLv = minLv;
	}

	public int getMaxLv() {
		return maxLv;
	}

	public void setMaxLv(int maxLv) {
		this.maxLv = maxLv;
	}
	
	public String getViplevelrange() {
		return viplevelrange;
	}

	public void setViplevelrange(String viplevelrange) {
		this.viplevelrange = viplevelrange;
		String[] lv = viplevelrange.split("\\+");
		minVipLv = Integer.parseInt(lv[0]);
		maxVipLv = Integer.parseInt(lv[1]);
	}

	public int getMinVipLv() {
		return minVipLv;
	}

	public void setMinVipLv(int minVipLv) {
		this.minVipLv = minVipLv;
	}

	public int getMaxVipLv() {
		return maxVipLv;
	}

	public void setMaxVipLv(int maxVipLv) {
		this.maxVipLv = maxVipLv;
	}
	
	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
		String[] rewardStr = reward.split(",");
		rewardMap = new HashMap<Integer, EverydayChargeRewardVo>();
		for (int i = 0; i < rewardStr.length; i++) {
			EverydayChargeRewardVo rewardVo = new EverydayChargeRewardVo(i + 1, rewardStr[i]);
			rewardMap.put(rewardVo.getId(), rewardVo);
		}
	}

	public Map<Integer, EverydayChargeRewardVo> getRewardMap() {
		return rewardMap;
	}

	public void setRewardMap(Map<Integer, EverydayChargeRewardVo> rewardMap) {
		this.rewardMap = rewardMap;
	}

	public int getOperateactid() {
		return operateactid;
	}

	public void setOperateactid(int operateactid) {
		this.operateactid = operateactid;
	}

	public int getEverydayid() {
		return everydayid;
	}

	public void setEverydayid(int everydayid) {
		this.everydayid = everydayid;
	}

	public boolean isSuitLv(int lv) {
		return lv >= minLv && lv <= maxLv;
	}
	
	public boolean isSuitVipLv(int vipLv) {
		return vipLv >= minVipLv && vipLv <= maxVipLv;
	}
}
