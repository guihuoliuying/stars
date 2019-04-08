package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashMap;
import java.util.Map;

public class NewFighterToFightActor extends Packet {

	private int fightingId;
	private int actorid;

	private String fightId;

	private byte[] data;
	//标识玩家状态, 由业务自己定义;
	private Map<Long, Byte> fightersMap;
	
	public NewFighterToFightActor(){
		
	}

	@Override
	public short getType() {
		return PacketDefine.ADD_FIGHTER_TO_FIGHTACTOR;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(actorid);
		buff.writeByte((byte)fightersMap.size());
		for(Map.Entry<Long, Byte> kvp : fightersMap.entrySet()){
			buff.writeLong(kvp.getKey());
			buff.writeByte(kvp.getValue());
		}
		buff.writeBytes(data);
		
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		actorid = buff.readInt();
		fightersMap = new HashMap<>();
		byte size = buff.readByte();
		for (int i = 0; i < size; i++) {
			fightersMap.put(buff.readLong(), buff.readByte());
		}
		data = new byte[buff.getBuff().readableBytes()];
		buff.getBuff().readBytes(data);
	}

	@Override
	public void execPacket() {

	}

	public int getActorid() {
		return actorid;
	}

	public void setActorid(int actorid) {
		this.actorid = actorid;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getFightingId() {
		return fightingId;
	}

	public void setFightingId(int fightingId) {
		this.fightingId = fightingId;
	}

	public String getFightId() {
		return fightId;
	}

	public void setFightId(String fightId) {
		this.fightId = fightId;
	}

	public Map<Long, Byte> getFightersMap() {
		return fightersMap;
	}

	public void setFightersMap(Map<Long, Byte> fightersMap) {
		this.fightersMap = fightersMap;
	}
}
