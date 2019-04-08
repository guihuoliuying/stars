package com.stars.modules.skyrank.prodata;

import com.stars.util.DateUtil;

/**
 * 
 * 天梯赛季时间
 * 
 * @author xieyuejun
 *
 */
public class SkyRankSeasonVo implements Comparable<SkyRankSeasonVo>{

	// skyranktimeid 赛季id
	// name 字符串，赛季名称
	// rewardtime 整值，赛季奖励结算时间点于结束时间点的时间差，单位秒
	// finishtime 赛季结束时间点，格式为：
	// yyyy-mm-dd hh:mm:ss

	private int skyRankTimeid;// 赛季id
	private String name;// 赛季名称
	private int rewardTime; // 赛季奖励结算时间点于结束时间点的时间差，单位秒
	private String finishTime; // 赛季结束时间点 yyyy-mm-dd hh:mm:ss
	
	
	private long lockedTime;
	private long sendAwardTime;
	private long finishedTime;
	
	public static long AWARD_INTERVAL =  2*60*1000;//发奖离锁积分间隔
	
	public void init(){
		this.finishedTime = DateUtil.toDate(finishTime).getTime();
		this.lockedTime = this.finishedTime - rewardTime*1000;
		this.setSendAwardTime(this.lockedTime + AWARD_INTERVAL);
	}

	public int getSkyRankTimeid() {
		return skyRankTimeid;
	}

	public void setSkyRankTimeid(int skyRankTimeid) {
		this.skyRankTimeid = skyRankTimeid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRewardTime() {
		return rewardTime;
	}

	public void setRewardTime(int rewardTime) {
		this.rewardTime = rewardTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public long getSendAwardTime() {
		return sendAwardTime;
	}

	public void setSendAwardTime(long sendAwardTime) {
		this.sendAwardTime = sendAwardTime;
	}

	public long getLockedTime() {
		return lockedTime;
	}

	public void setLockedTime(long lockedTime) {
		this.lockedTime = lockedTime;
	}
	
	

	public long getFinishedTime() {
		return finishedTime;
	}

	public void setFinishedTime(long finishedTime) {
		this.finishedTime = finishedTime;
	}

	@Override
	public int compareTo(SkyRankSeasonVo o) {
		return this.getSkyRankTimeid() -o.getSkyRankTimeid();
	}

}
