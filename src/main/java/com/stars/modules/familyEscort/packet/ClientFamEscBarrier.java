package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientFamEscBarrier extends Packet {
	
	private byte op;
	
	public ClientFamEscBarrier(){}
	
	public ClientFamEscBarrier(byte op){
		this.op = op;
	}

	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_BARRIER_INFO;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(op);
		//op == 0,增加障碍物  op==1,移除障碍物
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
