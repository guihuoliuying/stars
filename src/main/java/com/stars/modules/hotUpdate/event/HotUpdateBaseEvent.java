package com.stars.modules.hotUpdate.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateBaseEvent extends Event {

    private Object[] data;

    public HotUpdateBaseEvent(Object[] data) {
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }
}
