package com.stars.modules.pk.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 * 
 * 游戏服发到连接服的，用于告诉连接服，以后对应的客户端连接ID发送的数据，需要转发到对应的战斗服
 *
 */
public class ModifyConnectorRoute extends Packet {
	
	private int serverId;
	
	private long roleId;
	
	public ModifyConnectorRoute(){
	}
	
	
	@Override
	public short getType() {
		return 0X7F15;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(serverId);
		buff.writeLong(roleId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

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

	public long getRoleId() {
		return roleId;
	}


	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
}
