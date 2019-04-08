package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendDelFriendEvent extends Event {

    private long friendId;

    public FriendDelFriendEvent(long friendId) {
        this.friendId = friendId;

    }

    public long getFriendId() {
        return friendId;
    }
}
