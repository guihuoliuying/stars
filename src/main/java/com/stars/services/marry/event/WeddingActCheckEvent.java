package com.stars.services.marry.event;

import com.stars.core.event.Event;

/**
 * Created by zhoujin on 2017/4/13.
 */
public class WeddingActCheckEvent extends Event {

    private byte iconFlag;

    public WeddingActCheckEvent(byte iconFlag) {
        this.iconFlag = iconFlag;
    }

    public byte getIconFlag() {
        return this.iconFlag;
    }
}
