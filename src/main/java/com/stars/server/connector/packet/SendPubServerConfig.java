package com.stars.server.connector.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.connector.BackendAddress;
import com.stars.server.connector.Connector;

import java.util.ArrayList;
import java.util.List;

public class SendPubServerConfig extends Packet {
	
	List<com.stars.server.connector.BackendAddress>list;
	
	public SendPubServerConfig(){
		list = new ArrayList<com.stars.server.connector.BackendAddress>();
	}
	
	public void addBackendAddress(int id,String ip,int port) {
		list.add(new com.stars.server.connector.BackendAddress(id, ip, port));
	}

	@Override
	public short getType() {
		return Connector.PROTO_PUBLIC_CONFIG;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		byte size = (byte)list.size();
		buff.writeByte(size);
		for (BackendAddress backendAddress : list) {
			buff.writeInt(backendAddress.getServerId());
			buff.writeString(backendAddress.getIp());
			buff.writeInt(backendAddress.getPort());
		}
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
