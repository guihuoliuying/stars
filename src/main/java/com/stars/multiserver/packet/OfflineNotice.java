package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *掉线通知
 */
public class OfflineNotice extends Packet {
	
	private long roleId;
	
	private String key;
	
	public OfflineNotice(){
		
	}
	
	public OfflineNotice(long roleId,String key){
		this.roleId = roleId;
		this.key = key;
	}

	@Override
	public short getType() {
		return PacketDefine.OFFLINE_NOTICE;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeLong(roleId);
		buff.writeString(key);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		roleId = buff.readLong();
		key = buff.readString();
	}

	@Override
	public void execPacket() {

	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
