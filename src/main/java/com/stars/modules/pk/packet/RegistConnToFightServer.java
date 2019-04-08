package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *
 *主服注册连接到战斗服
 *主服建立连接到战斗服后，需要发送此包注册自己的身份
 *
 */
public class RegistConnToFightServer extends Packet {
	
	private int serverId;
	
	public RegistConnToFightServer(){
		
	}
	
	public RegistConnToFightServer(int serverId){
		this.serverId = serverId;
	}

	public short getType() {
		return PKPacketSet.RegistConn_To_FightServer;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(serverId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		serverId = buff.readInt();
	}

	@Override
	public void execPacket() {

	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

}
