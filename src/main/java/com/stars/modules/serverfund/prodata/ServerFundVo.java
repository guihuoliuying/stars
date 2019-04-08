package com.stars.modules.serverfund.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

public class ServerFundVo implements Comparable<ServerFundVo> {
	private int fundid;
	private int level;
	private int reward;
	private int operateactid;

	public int getFundid() {
		return fundid;
	}

	public void setFundid(int fundid) {
		this.fundid = fundid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public int getOperateactid() {
		return operateactid;
	}

	public void setOperateactid(int operateactid) {
		this.operateactid = operateactid;
	}

	@Override
	public int compareTo(ServerFundVo o) {
		return o.getFundid() - this.fundid;
	}

	public void writeToBuff(NewByteBuffer buff) {
		buff.writeInt(fundid);//基金id
		buff.writeInt(level);//领取等级
		buff.writeInt(reward);//掉落id
	}
}
