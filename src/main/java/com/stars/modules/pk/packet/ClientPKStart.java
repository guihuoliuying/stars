package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientPKStart extends Packet {

	@Override
	public short getType() {
		return PKPacketSet.Client_PK_START;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}

	@Override
	public void execPacket() {

	}

}
