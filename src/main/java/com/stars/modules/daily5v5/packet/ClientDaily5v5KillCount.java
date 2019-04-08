package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientDaily5v5KillCount extends PlayerPacket {
	
	private int killCount;
	
	public ClientDaily5v5KillCount() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientDaily5v5KillCount(int killCount) {
		this.killCount = killCount;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5KillCount;
	}
	
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(killCount);
    }

}
