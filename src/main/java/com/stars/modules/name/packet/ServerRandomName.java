package com.stars.modules.name.packet;

import com.stars.modules.name.NameModule;
import com.stars.modules.name.NamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ServerRandomName extends Packet {
	
	public ServerRandomName(){}
	

	@Override
	public void execPacket() {
		ClientRandomName crd = new ClientRandomName(NameModule.randomName());
		send(crd);
	}

	public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
    }
	
	@Override
	public short getType() {
		return NamePacketSet.Server_Req_Name;
	}
	public  void writeToBuffer(NewByteBuffer buff){}

}
