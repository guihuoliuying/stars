package com.stars.modules.skyrank.prodata;

/**
 * 赛季排名奖励（跨服）
 * 
 * @author xieyuejun
 *
 */
public class SkyRankSeasonRankAwardVo implements Comparable<SkyRankSeasonRankAwardVo> {
	private int lower;
	private int upper;
	private int dropId;

	public int getLower() {
		return lower;
	}

	public void setLower(int lower) {
		this.lower = lower;
	}

	public int getUpper() {
		return upper;
	}

	public void setUpper(int upper) {
		this.upper = upper;
	}

	public int getDropId() {
		return dropId;
	}

	public void setDropId(int dropId) {
		this.dropId = dropId;
	}

	@Override
	public int compareTo(SkyRankSeasonRankAwardVo o) {
		return o.getLower() - this.getLower();
	}

}
