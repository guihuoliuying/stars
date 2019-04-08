package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientFamilyEscortClearTips extends Packet {

	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_CLEARUP;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}
	
}
