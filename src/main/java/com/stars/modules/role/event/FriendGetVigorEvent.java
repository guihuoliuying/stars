package com.stars.modules.role.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2016/11/15.
 */
public class FriendGetVigorEvent extends Event {
    private int vigor;

    public FriendGetVigorEvent(int vigor) {
        this.vigor = vigor;
    }

    public int getVigor() {
        return vigor;
    }
}
