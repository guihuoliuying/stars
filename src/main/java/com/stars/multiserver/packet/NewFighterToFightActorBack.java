package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashSet;

public class NewFighterToFightActorBack extends Packet {
	
	private String key;
	private HashSet<Long>fighters;
	public NewFighterToFightActorBack(){
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return PacketDefine.ADD_FIGHTER_BACK;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(key);
		buff.writeByte((byte)fighters.size());
		for (Long b : fighters) {
			buff.writeLong(b);
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		key = buff.readString();
		fighters = new HashSet<Long>();
		byte size = buff.readByte();
		for (int i = 0; i < size; i++) {
			fighters.add(buff.readLong());
		}
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public HashSet<Long> getFighters() {
		return fighters;
	}

	public void setFighters(HashSet<Long> fighters) {
		this.fighters = fighters;
	}


}
