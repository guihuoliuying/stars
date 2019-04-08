package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ClientFamilyEscortMainUI extends Packet {

	private int es_count;
	private int rob_count;
	private int leftRobBaseCount;
	private int es_award;
	private int rob_award;
	
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_MAINUI;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(es_count);
		buff.writeInt(rob_count);
		buff.writeInt(leftRobBaseCount);
		buff.writeInt(es_award);
		buff.writeInt(rob_award);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public int getEs_count() {
		return es_count;
	}

	public void setEs_count(int es_count) {
		this.es_count = es_count;
	}

	public int getRob_count() {
		return rob_count;
	}

	public void setRob_count(int rob_count) {
		this.rob_count = rob_count;
	}

	public int getEs_award() {
		return es_award;
	}

	public void setEs_award(int es_award) {
		this.es_award = es_award;
	}

	public int getRob_award() {
		return rob_award;
	}

	public void setRob_award(int rob_award) {
		this.rob_award = rob_award;
	}

	public int getLeftRobBaseCount() {
		return leftRobBaseCount;
	}

	public void setLeftRobBaseCount(int leftRobBaseCount) {
		this.leftRobBaseCount = leftRobBaseCount;
	}
	
	

}
