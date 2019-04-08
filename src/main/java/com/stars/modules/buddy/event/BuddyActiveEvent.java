package com.stars.modules.buddy.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/8/31.
 */
public class BuddyActiveEvent extends Event {
    private int buddyId;

    public BuddyActiveEvent(int buddyId) {
        this.buddyId = buddyId;
    }

    public int getBuddyId() {
        return buddyId;
    }

    public void setBuddyId(int buddyId) {
        this.buddyId = buddyId;
    }
}
