package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2016/11/25.
 */
public class FriendRemoveApplyEvent extends Event {
    private long friendId;

    public FriendRemoveApplyEvent(long friendId) {
        this.friendId = friendId;
    }

    public long getFriendId() {
        return friendId;
    }
}
