package com.stars.services;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.core.actor.invocation.ServiceActor;

public abstract class ServicePacket extends Packet {
	
	private ServiceActor ServiceActor;

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

	public ServiceActor getServiceActor() {
		return ServiceActor;
	}

	public void setServiceActor(ServiceActor serviceActor) {
		ServiceActor = serviceActor;
	}

}
