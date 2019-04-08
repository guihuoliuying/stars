package com.stars.modules.skyrank.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 
 * 天梯奖励(只是用来读取数据库数据，需要进一步分解)
 * 
 * @author xieyuejun
 *
 */
public class SkyRankAwardVo {
	
	public static final byte TYPE_GRAD = 1; //段位奖励
	public static final byte TYPE_RANK = 2;//跨服排行奖励
	public static final byte TYPE_GRADUP = 3;//段位升级奖励
	public static final byte TYPE_DAILY = 4;//每日奖励
	
	// skyrankrewardid 天梯奖励唯一标识
	// type 整值，奖励类型。1=段位赛季奖励，2=跨服榜赛季奖励。3=段位升级奖励
	// param 字符串，奖励参数，和类型对应，1= skyrankgradid，2=排名下限+排名上限。1= skyrankgradid
	// reward dropid
	private int skyRankRewardId;
	private byte type;
	private String param;
	private int reward;
	
	public void writeBuffer(NewByteBuffer buff) {
		buff.writeInt(skyRankRewardId);
		buff.writeInt(type);
		buff.writeString(param);
		buff.writeInt(reward);
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public int getSkyRankRewardId() {
		return skyRankRewardId;
	}

	public void setSkyRankRewardId(int skyRankRewardId) {
		this.skyRankRewardId = skyRankRewardId;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

}
