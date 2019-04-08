package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * 运镖场景玩家状态信息
 * 
 * @author xieyuejun
 *
 */
public class ClientEscortPlayerStatus extends Packet  {

	private Map<Long,Byte> playerStatusMap = new HashMap<>();
	
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_PLAYERSTATUS;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		int size = playerStatusMap.size();
		buff.writeInt(size);
		if (size > 0) {
			for (Entry<Long, Byte> entry : playerStatusMap.entrySet()) {
				buff.writeString(entry.getKey()+"");
				buff.writeByte(entry.getValue());
			}
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

	public Map<Long, Byte> getPlayerStatusMap() {
		return playerStatusMap;
	}

	public void setPlayerStatusMap(Map<Long, Byte> playerStatusMap) {
		this.playerStatusMap = playerStatusMap;
	}
	
	

}
