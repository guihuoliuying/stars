package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class FriendAddVigorEvent extends Event {
    private long friendId;

    public FriendAddVigorEvent(long friendId) {
        this.friendId = friendId;
    }

    public long getFriendId() {
        return friendId;
    }
}
