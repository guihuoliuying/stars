package com.stars.multiserver.familywar.event;

import com.stars.core.event.Event;
import com.stars.network.server.packet.Packet;

public class FamilyWarSendPacketEvent extends Event {
	private Packet packet;
	private byte sceneType;

	public FamilyWarSendPacketEvent(Packet packet, byte sceneType) {
		this.packet = packet;
		this.sceneType = sceneType;
	}
	
	public Packet getPacket() {
		return packet;
	}

	public void setPacket(Packet packet) {
		this.packet = packet;
	}

	public byte getSceneType() {
		return sceneType;
	}

	public void setSceneType(byte sceneType) {
		this.sceneType = sceneType;
	}

}
