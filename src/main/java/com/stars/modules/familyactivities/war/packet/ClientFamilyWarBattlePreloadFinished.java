package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientFamilyWarBattlePreloadFinished extends PlayerPacket {
	
	private int remainderTime;
	
	public ClientFamilyWarBattlePreloadFinished() {
		
	}
	
	public ClientFamilyWarBattlePreloadFinished(int remainderTime) {
		this.remainderTime = remainderTime;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(remainderTime);
	}

	@Override
	public short getType() {
		return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_PRELOAD_FINISHED;
	}

	public int getRemainderTime() {
		return remainderTime;
	}

	public void setRemainderTime(int remainderTime) {
		this.remainderTime = remainderTime;
	}

}
