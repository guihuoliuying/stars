package com.stars.modules.friend.event;

import com.stars.core.event.Event;

/**
 * Created by wuyuxing on 2017/2/11.
 */
public class FriendAchieveEvent extends Event {
    private int friendNum;

    public int getFriendNum() {
        return friendNum;
    }

    public FriendAchieveEvent(int friendNum) {

        this.friendNum = friendNum;
    }
}
