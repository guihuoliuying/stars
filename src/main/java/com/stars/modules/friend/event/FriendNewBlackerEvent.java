package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendNewBlackerEvent extends Event {

    private long blackerId;

    public FriendNewBlackerEvent(long blackerId) {
        this.blackerId = blackerId;
    }

    public long getBlackerId() {
        return blackerId;
    }
}
