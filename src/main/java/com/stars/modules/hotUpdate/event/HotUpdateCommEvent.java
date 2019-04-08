package com.stars.modules.hotUpdate.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateCommEvent extends Event {

    private Object[] data;

    public HotUpdateCommEvent(Object[] data) {
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }
}
