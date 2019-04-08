package com.stars.modules.authentic.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017/2/7 15:57
 */
public class AuthenticEvent extends Event {
    private int type;
    private int times;

    public AuthenticEvent(int type, int times) {
        this.type = type;
        this.times = times;
    }

    public int getType() {
        return type;
    }

    public int getTimes() {
        return times;
    }
}
