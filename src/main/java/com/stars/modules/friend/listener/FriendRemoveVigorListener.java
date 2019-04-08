package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendRemoveVigorEvent;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class FriendRemoveVigorListener implements EventListener {
    private FriendModule  module;
    public FriendRemoveVigorListener(FriendModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        FriendRemoveVigorEvent vigorEvent = (FriendRemoveVigorEvent) event;
        module.innerRemoveVigorList(vigorEvent.getFriendId());
    }
}
