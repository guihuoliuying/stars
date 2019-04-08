package com.stars.modules.skyrank.packet;

import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 天梯玩家获得的段位奖励下发
 * @author xieyuejun
 *
 */
public class ClientSkyRankGradAwardData extends Packet {
	
	public static final byte GRAD_UP_AWARD = 1;//段位升级奖励
	public static final byte GET_DAILY_AWARD = 2;//领取每日奖励结果

	private Map<Integer, Integer> awarToolMap;
	private byte opType;
	private byte getResult;
	
	@Override
	public short getType() {
		return SkyRankPacketSet.ClientSkyRankGradAwardData;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeByte(opType);
		if(opType==GRAD_UP_AWARD){
			int size = awarToolMap == null?0:awarToolMap.size();
			buff.writeInt(size);
			if(size >0){
				for(Entry<Integer, Integer> entry:awarToolMap.entrySet()){
					buff.writeInt(entry.getKey());
					buff.writeInt(entry.getValue());
				}
			}
		}else if(opType==GET_DAILY_AWARD){
			buff.writeInt(getResult);//1 成功
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public Map<Integer, Integer> getAwarToolMap() {
		return awarToolMap;
	}

	public void setAwarToolMap(Map<Integer, Integer> awarToolMap) {
		this.awarToolMap = awarToolMap;
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getGetResult() {
		return getResult;
	}

	public void setGetResult(byte getResult) {
		this.getResult = getResult;
	}
	
}
