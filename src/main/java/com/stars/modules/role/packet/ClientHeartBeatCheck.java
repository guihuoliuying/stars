package com.stars.modules.role.packet;

import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientHeartBeatCheck extends Packet {
	
	private String key;
	
	private int delay;

	public ClientHeartBeatCheck(){
	}
	
	public ClientHeartBeatCheck(String key,int delay){
		this.key = key;
		this.delay = delay;
	}

	@Override
	public short getType() {
		return RolePacketSet.C_HEARTBEAT_CHECK;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeString(key);
		buff.writeInt(delay);
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
