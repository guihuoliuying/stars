package com.stars.multiserver.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *请求战斗发起一场战斗，用于业务服与战斗服双向通信
 *
 */
public class CreateFightActorReq extends Packet {
	
	private int connId;
	private int actorId;
	private byte[] fightData;
	private String key;
	
	public CreateFightActorReq(){
		
	}

	@Override
	public short getType() {
		return PacketDefine.CREATE_FIGHTACTOR_REQ;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(connId);
		buff.writeInt(actorId);
		buff.writeString(key);
		buff.writeBytes(fightData);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		connId = buff.readInt();
		actorId = buff.readInt();
		key = buff.readString();
		fightData = new byte[buff.getBuff().readableBytes()];
		buff.getBuff().readBytes(fightData);
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}


	public byte[] getFightData() {
		return fightData;
	}

	public void setFightData(byte[] fightData) {
		this.fightData = fightData;
	}

//
//	public void setFighters(long[] fighters) {
//		this.fighters = fighters;
//	}

	public int getActorId() {
		return actorId;
	}

	public void setActorId(int actorId) {
		this.actorId = actorId;
	}

	public int getConnId() {
		return connId;
	}

	public void setConnId(int connId) {
		this.connId = connId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
