package com.stars.modules.daily5v5.event;

import com.stars.core.event.Event;
import com.stars.modules.daily5v5.packet.ClientDaily5v5;

public class Daily5v5FightEndEvent extends Event {
	
	private ClientDaily5v5 packet;
	
	private byte result;
	
	private int time;

	public ClientDaily5v5 getPacket() {
		return packet;
	}

	public void setPacket(ClientDaily5v5 packet) {
		this.packet = packet;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
