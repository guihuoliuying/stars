package com.stars.modules.hotUpdate.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2017/1/4.
 */
public class HotUpdateStandyEvent extends Event {

    private Object[] data;

    public HotUpdateStandyEvent(Object[] data) {
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }

}
