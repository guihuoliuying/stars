package com.stars.modules.archery.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.archery.ArcheryManager;
import com.stars.modules.archery.ArcheryPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class ClientArchery extends PlayerPacket {
	
	public ClientArchery() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientArchery(byte opType) {
		this.opType = opType;
	}
	
	private byte opType;
	
	private byte leftTimes;
	
	private Map<Integer, Integer> award;
	
	@Override
	public short getType() {
		return ArcheryPacketSet.CLIENT_ARCHERY;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(opType);
		if(opType==ArcheryManager.PLAY_TIMES){
			buff.writeByte(leftTimes);//剩余次数
			buff.writeByte(ArcheryManager.TotalPlayNum);//总次数
		}else if(opType==ArcheryManager.SHOW_AWARD){
			int awardSize = award.size();
			buff.writeByte((byte)awardSize);
			for(int awardItem : award.keySet()){
				buff.writeInt(awardItem);//道具id
				buff.writeInt(award.get(awardItem));//数量
			}
		}
	}
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getLeftTimes() {
		return leftTimes;
	}

	public void setLeftTimes(byte leftTimes) {
		this.leftTimes = leftTimes;
	}

	public Map<Integer, Integer> getAward() {
		return award;
	}

	public void setAward(Map<Integer, Integer> award) {
		this.award = award;
	}

}
