package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

public class ClientFamilyWarBattleFightBlock extends PlayerPacket {
	
	private Map<String, Byte> blockMap;
	
	public ClientFamilyWarBattleFightBlock(Map<String, Byte> blockMap) {
		this.blockMap = blockMap;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_BLOCK;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		short size = (short) (blockMap == null ? 0 : blockMap.size());
        buff.writeShort(size);
        if (blockMap != null) {
            for (Map.Entry<String, Byte> entry : blockMap.entrySet()) {
                buff.writeString(entry.getKey());
                buff.writeByte(entry.getValue());
            }
        }
	}

	public Map<String, Byte> getBlockMap() {
		return blockMap;
	}

	public void setBlockMap(Map<String, Byte> blockMap) {
		this.blockMap = blockMap;
	}

}
