package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.familyEscort.route.EscortCar;

public class ClientEscCarFlush extends Packet {
	
	private EscortCar car;
	
	private byte op; // 0 刷新状态  1 删除镖车

	public ClientEscCarFlush(){}
	
	public ClientEscCarFlush(byte op,EscortCar car){
		this.car = car;
		this.op = op;
	}
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_CAR_FLUSH;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeString(String.valueOf(car.getRoleId()));
		buff.writeByte(op);
		if (op == 0) { //刷新状态
			buff.writeByte(car.getRunStatus());
		}else if (op == 1) {//删除镖车
			
		}
		
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}

	@Override
	public void execPacket() {

	}

}
