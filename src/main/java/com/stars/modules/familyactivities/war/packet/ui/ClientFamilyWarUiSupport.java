package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientFamilyWarUiSupport extends PlayerPacket {

	private long familyId;
	
	public ClientFamilyWarUiSupport() {
		
	}
	
	public ClientFamilyWarUiSupport(long familyId) {
		this.familyId = familyId;
	}
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(Long.toString(familyId));
	}

	@Override
	public short getType() {
		return FamilyActWarPacketSet.C_FAMILY_WAR_UI_SUPPORT;
	}

	public long getFamilyId() {
		return familyId;
	}

	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}
	
}
