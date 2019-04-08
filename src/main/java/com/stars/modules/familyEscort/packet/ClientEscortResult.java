package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.LinkedList;
import java.util.List;

public class ClientEscortResult extends Packet{
	
	private int escortCount;
	private byte startCount;
	private int escortAward;
	private List<Integer> extAward  = new LinkedList<>();
	

	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_RESULT;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(escortCount);
		buff.writeByte(startCount);
		buff.writeInt(escortAward);
		byte size  = (byte) extAward.size();
		buff.writeByte(size);
		if(size >0){
			for(Integer exta:extAward){
				buff.writeInt(exta);
			}
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public byte getStartCount() {
		return startCount;
	}

	public void setStartCount(byte startCount) {
		this.startCount = startCount;
	}

	public int getEscortAward() {
		return escortAward;
	}

	public void setEscortAward(int escortAward) {
		this.escortAward = escortAward;
	}

	public List<Integer> getExtAward() {
		return extAward;
	}

	public void setExtAward(List<Integer> extAward) {
		this.extAward = extAward;
	}

	public int getEscortCount() {
		return escortCount;
	}

	public void setEscortCount(int escortCount) {
		this.escortCount = escortCount;
	}
	
	

}
