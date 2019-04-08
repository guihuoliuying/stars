package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendDelFriendEvent;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendDelFriendListener implements EventListener {

    private FriendModule module;

    public FriendDelFriendListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendDelFriendEvent friendEvent = (FriendDelFriendEvent) event;
        module.innerDelFriend(friendEvent.getFriendId());
    }
}
