package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class CreateFightActorBack extends Packet {

	private int fightActorId;
	
	private int actorId;
	
	private String key;
	
	public CreateFightActorBack(){
		
	}
	
	@Override
	public short getType() {
		return PacketDefine.CREATE_FIGHTACTOR_BACK;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(fightActorId);
		buff.writeString(key);
		buff.writeInt(actorId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		fightActorId = buff.readInt();
		key = buff.readString();
		actorId = buff.readInt();
	}

	@Override
	public void execPacket() {

	}

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getFightActorId() {
		return fightActorId;
	}

	public void setFightActorId(int fightActorId) {
		this.fightActorId = fightActorId;
	}

}
