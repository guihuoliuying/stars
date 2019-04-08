package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendNewFriendEvent;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendNewFriendListener implements EventListener {

    private FriendModule module;

    public FriendNewFriendListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendNewFriendEvent friendEvent = (FriendNewFriendEvent) event;
        module.innerNewFriend(friendEvent.getFriendId());
    }
}
