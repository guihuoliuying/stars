package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/25.
 */
public class FriendApplyAddEvent extends Event {
    private long friendId;

    public FriendApplyAddEvent(long friendId) {
        this.friendId = friendId;
    }

    public long getFriendId() {
        return friendId;
    }
}
