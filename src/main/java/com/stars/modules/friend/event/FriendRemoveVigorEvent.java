package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class FriendRemoveVigorEvent extends Event {
    private long friendId;

    public FriendRemoveVigorEvent(long friendId) {
        this.friendId = friendId;
    }

    public long getFriendId() {
        return friendId;
    }
}
