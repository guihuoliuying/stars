package com.stars.modules.pk.packet;

import com.stars.modules.pk.PKPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *
 *PK结束后，战斗服返回给游戏服的战斗结果
 *
 */
public class PVPResultPacket extends Packet {

	private long victor;
	
	private long loser;

	private long invitor;
	
	public PVPResultPacket(){
		
	}
	
	@Override
	public short getType() {
		return PKPacketSet.PVP_RESULT;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		buff.writeLong(victor);
		buff.writeLong(loser);
		buff.writeLong(invitor);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		victor = buff.readLong();
		loser = buff.readLong();
		invitor = buff.readLong();
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

	public long getVictor() {
		return victor;
	}

	public void setVictor(long victor) {
		this.victor = victor;
	}

	public long getLoser() {
		return loser;
	}

	public void setLoser(long loser) {
		this.loser = loser;
	}

	public long getInvitor() {
		return invitor;
	}

	public void setInvitor(long invitor) {
		this.invitor = invitor;
	}
}
