package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;

public class ClientFamilyWarBattleStartTips extends PlayerPacket {

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_START_TIPS;
	}

}
