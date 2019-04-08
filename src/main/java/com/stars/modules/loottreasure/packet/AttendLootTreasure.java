package com.stars.modules.loottreasure.packet;

import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * @author dengzhou
 *进入夺宝服务的双向包（主服-夺宝服）
 *
 */
public class AttendLootTreasure extends Packet {
	
	private FighterEntity fEntity;
	private int id;
	private int serverId;
	private String serverName;
	private int jobId;

	public AttendLootTreasure(){
		
	}
	
	
	public AttendLootTreasure(int serverId, String serverName, int id,FighterEntity fEntity, int jobId){
		this.fEntity = fEntity;
		this.id = id;
		this.serverId = serverId;
		this.serverName = serverName;
		this.jobId = jobId;
	}
	

	@Override
	public short getType() {
		return LootTreasurePacketSet.ATTEND_LOOTTREASURE;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(serverId);
		buff.writeString(serverName);
		buff.writeInt(id);
		fEntity.writeToBuff(buff);
		buff.writeInt(jobId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		serverId = buff.readInt();
		serverName = buff.readString();
		id = buff.readInt();
		fEntity = new FighterEntity(buff.readByte(), buff.readString());
		fEntity.readFromBuff(buff);
		jobId = buff.readInt();
	}

	@Override
	public void execPacket() {

	}


	public FighterEntity getfEntity() {
		return fEntity;
	}


	public void setfEntity(FighterEntity fEntity) {
		this.fEntity = fEntity;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getServerId() {
		return serverId;
	}


	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
}
