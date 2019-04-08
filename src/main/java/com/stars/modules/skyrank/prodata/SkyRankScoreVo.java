package com.stars.modules.skyrank.prodata;

/**
 * 
 * 天梯积分获取规则配置
 * 
 * @author xieyuejun
 *
 */
public class SkyRankScoreVo {
	
	
	public static final short TYPE_OFFLINEPVP = 1;//演武场
	public static final short TYPE_KFPVP = 2;//演武场
	public static final short TYPE_5V5PVP = 3;//日常5v5
	
	
	public static boolean SCORE_SWITCH_ALL = true;//总开关
	
	public static boolean SCORE_SWITCH_OFFLINEPVP = true;
	public static boolean SCORE_SWITCH_KFPVP = true;
	public static boolean SCORE_SWITCH_5V5PVP = true;
	
	// 1=演武场
	// 2=演武场
	// 3=日常5v5

	// rankscoretype 天梯积分对应业务类型，当前主要有三类，后续新增
	// 1=演武场
	// 2=巅峰对决
	// 3=日常5v5
	// sucscore 整值，一场战斗胜利可以获得的天梯积分
	// failscore 整值，一场战斗失败可以获得的天梯积分
	// addmaxtimes 整值，每天可以在改业务下增加天梯积分的次数上限（包括+n和0）。
	// submaxtimes 整值，每天可以在该业务下产出的负分次数
	// （-n）
	// maxtimenotice 产出次数达到上限后的聊天频道提示
	// locknotice 积分产出被锁定的聊天频道提示

	private short rankScoreType;
	private int sucScore;
	private int failScore;
	private int maxTimes;
	private String maxtimeNotice;// 产出次数达到上限后的聊天频道提示
	private String lockNotice;// 积分产出被锁定的聊天频道提示

	public int getSucScore() {
		return sucScore;
	}

	public void setSucScore(int sucScore) {
		this.sucScore = sucScore;
	}

	public int getFailScore() {
		return failScore;
	}

	public void setFailScore(int failScore) {
		this.failScore = failScore;
	}

	public String getMaxtimeNotice() {
		return maxtimeNotice;
	}

	public void setMaxtimeNotice(String maxtimeNotice) {
		this.maxtimeNotice = maxtimeNotice;
	}

	public String getLockNotice() {
		return lockNotice;
	}

	public void setLockNotice(String lockNotice) {
		this.lockNotice = lockNotice;
	}

	public short getRankScoreType() {
		return rankScoreType;
	}

	public void setRankScoreType(short rankScoreType) {
		this.rankScoreType = rankScoreType;
	}

	public int getMaxTimes() {
		return maxTimes;
	}

	public void setMaxTimes(int maxTimes) {
		this.maxTimes = maxTimes;
	}

}
