package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientClearOtherFighters extends Packet {
	
	public ClientClearOtherFighters(){
		
	}

	@Override
	public short getType() {
		return PacketDefine.CLIENT_CLEAR_FIGHTERS;
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
