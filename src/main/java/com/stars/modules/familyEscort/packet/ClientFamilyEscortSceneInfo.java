package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientFamilyEscortSceneInfo extends Packet{

	private int stageId;
	private int stageType;//0 运镖 1 劫镖
	private String bornPos;
	private String tranferInfo;
	private int sceneRemainTime;
	private int sceneClearTime;
	private int startCount;
	private int leftEsCount;
	private int leftRobCount;
	private int leftRobBaseCount;
	private String enterFamilyId;
	
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_SCENE;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(stageId);
		buff.writeInt(stageType);
		buff.writeString(bornPos);
		buff.writeString(tranferInfo);
		buff.writeInt(sceneRemainTime);
		buff.writeInt(sceneClearTime);
		buff.writeInt(startCount);
		buff.writeInt(leftEsCount);
		buff.writeInt(leftRobCount);
		buff.writeInt(leftRobBaseCount);
		buff.writeString(enterFamilyId);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public int getStageType() {
		return stageType;
	}

	public void setStageType(int stageType) {
		this.stageType = stageType;
	}

	public String getTranferInfo() {
		return tranferInfo;
	}

	public void setTranferInfo(String tranferInfo) {
		this.tranferInfo = tranferInfo;
	}

	public int getSceneRemainTime() {
		return sceneRemainTime;
	}

	public void setSceneRemainTime(int sceneRemainTime) {
		this.sceneRemainTime = sceneRemainTime;
	}

	public int getStartCount() {
		return startCount;
	}

	public void setStartCount(int startCount) {
		this.startCount = startCount;
	}

	public String getBornPos() {
		return bornPos;
	}

	public void setBornPos(String bornPos) {
		this.bornPos = bornPos;
	}

	public int getLeftEsCount() {
		return leftEsCount;
	}

	public void setLeftEsCount(int leftEsCount) {
		this.leftEsCount = leftEsCount;
	}

	public int getLeftRobCount() {
		return leftRobCount;
	}

	public void setLeftRobCount(int leftRobCount) {
		this.leftRobCount = leftRobCount;
	}

	public String getEnterFamilyId() {
		return enterFamilyId;
	}

	public void setEnterFamilyId(String enterFamilyId) {
		this.enterFamilyId = enterFamilyId;
	}

	public int getLeftRobBaseCount() {
		return leftRobBaseCount;
	}

	public void setLeftRobBaseCount(int leftRobBaseCount) {
		this.leftRobBaseCount = leftRobBaseCount;
	}

	public int getSceneClearTime() {
		return sceneClearTime;
	}

	public void setSceneClearTime(int sceneClearTime) {
		this.sceneClearTime = sceneClearTime;
	}

}
