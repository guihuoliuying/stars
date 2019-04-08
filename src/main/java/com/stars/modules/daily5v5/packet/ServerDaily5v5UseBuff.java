package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerDaily5v5UseBuff extends PlayerPacket {
	
	private int effectId;
	
	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		effectId = buff.readInt();
	}

	@Override
	public void execPacket(Player player) {
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Server_Daily5v5UseBuff;
	}

	public int getEffectId() {
		return effectId;
	}

	public void setEffectId(int effectId) {
		this.effectId = effectId;
	}

}
