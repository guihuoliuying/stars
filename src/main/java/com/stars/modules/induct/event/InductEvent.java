package com.stars.modules.induct.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/3/28.
 */
public class InductEvent extends Event {
	private byte eventType;
    private int inductId;
    
    public InductEvent(byte eventType, int inductId) {
        this.eventType = eventType;
        this.inductId = inductId;
    }

    public byte getEventType() {
        return eventType;
    }

    public int getInductId() {
        return inductId;
    }
}
