package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * 
 * 运镖活动剩余时间提示
 * 
 * @author xieyuejun
 *
 */
public class ClientEscTimeTips extends Packet {
	private int sceneRemainTime;
	private int sceneClearTime;

	public ClientEscTimeTips() {
	}

	public ClientEscTimeTips(int sceneRemainTime, int sceneClearTime) {
		this.sceneClearTime = sceneClearTime;
		this.sceneRemainTime = sceneRemainTime;
	}

	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_TIMETIPS;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(sceneClearTime);
		buff.writeInt(sceneRemainTime);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}

	@Override
	public void execPacket() {

	}

	public int getSceneRemainTime() {
		return sceneRemainTime;
	}

	public void setSceneRemainTime(int sceneRemainTime) {
		this.sceneRemainTime = sceneRemainTime;
	}

	public int getSceneClearTime() {
		return sceneClearTime;
	}

	public void setSceneClearTime(int sceneClearTime) {
		this.sceneClearTime = sceneClearTime;
	}

}
