package com.stars.modules.daily5v5.event;

import com.stars.core.event.Event;

public class Daily5v5MessageEvent extends Event {
	
	private byte opType;

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

}
