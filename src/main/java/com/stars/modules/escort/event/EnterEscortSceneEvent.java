package com.stars.modules.escort.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EnterEscortSceneEvent extends Event {
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public EnterEscortSceneEvent(byte[] data) {
        this.data = data;
    }
}
