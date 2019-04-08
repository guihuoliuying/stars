package com.stars.modules.fightingmaster.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/3/14.
 */
public class FiveRewardStatusEvent extends Event {
    public static final byte CAN_NOT_GET = 0;
    public static final byte CAN_GET = 1;
    public static final byte HAVE_GOT = 2;
	
	
    private byte status;

    public FiveRewardStatusEvent(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }
}
