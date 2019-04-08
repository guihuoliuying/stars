package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendCanReceiveVigorEvent;

/**
 * Created by chenkeyu on 2016/12/10.
 */
public class FriendCanReceiveVigorListener implements EventListener {
    private FriendModule module;
    public FriendCanReceiveVigorListener(FriendModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        FriendCanReceiveVigorEvent vigorEvent = (FriendCanReceiveVigorEvent) event;
        module.setCanReceiveVigor(vigorEvent.isFlag());
    }
}
