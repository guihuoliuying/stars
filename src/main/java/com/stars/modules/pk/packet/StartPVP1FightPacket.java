package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *游戏服请求战斗服发起一场战斗
 *双向包
 */
public class StartPVP1FightPacket extends Packet {

	private int serverId;
	
	private byte[] initData;
	
	private long invitor;
	
	private long invitee;
	
	public StartPVP1FightPacket(){
		
	}
	
	@Override
	public short getType() {
		return PKPacketSet.Start_PVP_Request;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeInt(serverId);
		buff.writeLong(invitor);
		buff.writeLong(invitee);
		buff.writeBytes(initData);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.serverId = buff.readInt();
		this.invitor = buff.readLong();
		this.invitee = buff.readLong();
		this.initData = new byte[buff.getBuff().readableBytes()];
		buff.getBuff().readBytes(this.initData);
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public long getInvitor() {
		return invitor;
	}

	public void setInvitor(long invitor) {
		this.invitor = invitor;
	}

	public long getInvitee() {
		return invitee;
	}

	public void setInvitee(long invitee) {
		this.invitee = invitee;
	}

	public byte[] getInitData() {
		return initData;
	}

	public void setInitData(byte[] initData) {
		this.initData = initData;
	}
}
