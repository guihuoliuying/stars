package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.familyEscort.route.EscortCar;

import java.util.ArrayList;
import java.util.List;

public class ClientFamilyEscortCars extends Packet {
	
	private List<EscortCar>cars;
	
	public ClientFamilyEscortCars(){
		cars = new ArrayList<EscortCar>();
	}
	
	public ClientFamilyEscortCars(List<EscortCar>cars){
		this.cars = cars;
	}
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_CAR;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		byte size = (byte)cars.size();
		buff.writeByte(size);
		for (EscortCar escortCar : cars) {
			buff.writeString(String.valueOf(escortCar.getRoleId()));
			buff.writeString(escortCar.getName());
			buff.writeInt(escortCar.getRunTime());
			buff.writeByte(escortCar.getRunStatus());
			buff.writeInt(escortCar.getFightScore());
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {

	}

	@Override
	public void execPacket() {

	}
	
	public void addEscortCar(EscortCar car){
		cars.add(car);
	}

}
