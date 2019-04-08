package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendNewBlackerEvent;

/**
 * Created by zhaowenshuo on 2016/8/22.
 */
public class FriendNewBlackerListener implements EventListener {

    private FriendModule module;

    public FriendNewBlackerListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FriendNewBlackerEvent blackerEvent = (FriendNewBlackerEvent) event;
        module.innerNewBlacker(blackerEvent.getBlackerId());
    }
}
