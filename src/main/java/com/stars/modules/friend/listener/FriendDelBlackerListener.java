package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendDelBlackerEvent;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendDelBlackerListener implements EventListener {

    private FriendModule module;

    public FriendDelBlackerListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendDelBlackerEvent blackerEvent = (FriendDelBlackerEvent) event;
        module.innerDelBlacker(blackerEvent.getBlackerId());
    }
}
