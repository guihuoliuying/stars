package com.stars.modules.skyrank.packet;

import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 
 * 天梯赛季时间状态
 * @author xieyuejun
 *
 */
public class ClientSkyRankTimeDesc extends Packet {

	public static final byte TIME_TYPE_OPENSCORE = 1;//积分开启时间段
	public static final byte TIME_TYPE_CLOSESCORE = 2;//积分锁定时间段
	
	private byte timeType;
	private long timeStamp;
	private byte dailyAwardState;
	private int awardId;
	
	
	@Override
	public short getType() {
		return SkyRankPacketSet.ClientSkyRankTimeDesc;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeByte(timeType);
		buff.writeLong(timeStamp);
		buff.writeByte(dailyAwardState);
		buff.writeInt(awardId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public byte getTimeType() {
		return timeType;
	}

	public void setTimeType(byte timeType) {
		this.timeType = timeType;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public byte getDailyAwardState() {
		return dailyAwardState;
	}

	public void setDailyAwardState(byte dailyAwardState) {
		this.dailyAwardState = dailyAwardState;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}
	
}
