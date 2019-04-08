package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientDaily5v5PersonalPoint extends PlayerPacket {
	
	private long points;
	
	public ClientDaily5v5PersonalPoint() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientDaily5v5PersonalPoint(long points) {
		this.points = points;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(String.valueOf(points));
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5PersonalPoint;
	}

}
