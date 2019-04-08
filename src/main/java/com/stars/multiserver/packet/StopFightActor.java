package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class StopFightActor extends Packet {

	private int fightServerId;
	private int fromServerId;

	private int actorId;
	private String fightId;

	public StopFightActor(){}
	
	public StopFightActor(String fightId){
		this.fightId = fightId;
	}

	public StopFightActor(int fightServerId, int fromServerId) {
		this.fightServerId = fightServerId;
		this.fromServerId = fromServerId;
	}

	public StopFightActor(int actorId){
		this.actorId = actorId;
		fightId = String.valueOf(actorId);
	}

	public String getFightId() {
		return fightId;
	}

	@Override
	public short getType() {
		return PacketDefine.STOP_FIGHT_ACTOR;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(actorId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		actorId = buff.readInt();
	}

	@Override
	public void execPacket() {

	}

	public int getFightServerId() {
		return fightServerId;
	}

	public void setFightServerId(int fightServerId) {
		this.fightServerId = fightServerId;
	}

	public int getFromServerId() {
		return fromServerId;
	}

	public void setFromServerId(int fromServerId) {
		this.fromServerId = fromServerId;
	}
}
