package com.stars.modules.everydaycharge.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.everydaycharge.EverydayChargePacketSet;
import com.stars.modules.everydaycharge.prodata.EverydayChargeRewardVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;
import java.util.Set;

public class ClientEverydayCharge extends PlayerPacket {
	
	public static final byte RESP_OPEN_UI = 1;//打开界面
	public static final byte RESP_LOTTERY = 2;//开始抽奖
	
	private byte respType;
	private Map<Integer, EverydayChargeRewardVo> rewardVoMap;
	private Set<Integer> rewardRecord;
	private int dropId;
	private byte chargeState;
	private String timeDesc;
	private byte lotteryTimes;

	@Override
	public void execPacket(Player player) {
		
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(respType);
		if (respType == RESP_OPEN_UI) {
			buff.writeInt(rewardVoMap.size());
			for (EverydayChargeRewardVo rewardVo : rewardVoMap.values()) {
				buff.writeInt(rewardVo.getId());	//奖励id，可用作固定位置
				buff.writeInt(rewardVo.getDropid());//掉落id
			}
			buff.writeInt(rewardRecord.size());
			for (int record : rewardRecord) {
				buff.writeInt(record);//抽奖记录，记录掉落id
			}
			buff.writeByte(chargeState);//充值状态，0=未充值，1=已充值
			buff.writeString(timeDesc);//时间描述
			buff.writeByte(lotteryTimes);//剩余抽奖次数
		} else if (respType == RESP_LOTTERY) {
			buff.writeInt(dropId);//奖励id
		}
	}

	@Override
	public short getType() {
		return EverydayChargePacketSet.C_EVERYDAY_CHARGE;
	}

	public byte getRespType() {
		return respType;
	}

	public void setRespType(byte respType) {
		this.respType = respType;
	}

	public Map<Integer, EverydayChargeRewardVo> getRewardVoMap() {
		return rewardVoMap;
	}

	public void setRewardVoMap(Map<Integer, EverydayChargeRewardVo> rewardVoMap) {
		this.rewardVoMap = rewardVoMap;
	}

	public int getDropId() {
		return dropId;
	}

	public void setDropId(int dropId) {
		this.dropId = dropId;
	}

	public Set<Integer> getRewardRecord() {
		return rewardRecord;
	}

	public void setRewardRecord(Set<Integer> rewardRecord) {
		this.rewardRecord = rewardRecord;
	}

	public byte getChargeState() {
		return chargeState;
	}

	public void setChargeState(byte chargeState) {
		this.chargeState = chargeState;
	}

	public String getTimeDesc() {
		return timeDesc;
	}

	public void setTimeDesc(String timeDesc) {
		this.timeDesc = timeDesc;
	}

	public byte getLotteryTimes() {
		return lotteryTimes;
	}

	public void setLotteryTimes(byte lotteryTimes) {
		this.lotteryTimes = lotteryTimes;
	}

}
