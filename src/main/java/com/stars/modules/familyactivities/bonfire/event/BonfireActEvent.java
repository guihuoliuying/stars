package com.stars.modules.familyactivities.bonfire.event;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2016/10/11.
 */
public class BonfireActEvent extends Event {

    private int state;

    public BonfireActEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
