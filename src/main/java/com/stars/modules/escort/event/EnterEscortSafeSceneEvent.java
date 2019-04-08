package com.stars.modules.escort.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2016/12/9.
 */
public class EnterEscortSafeSceneEvent extends Event {
    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public EnterEscortSafeSceneEvent(byte[] data) {
        this.data = data;
    }
}
