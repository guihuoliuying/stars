package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendAddVigorEvent;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class FriendAddVigorListener implements EventListener {
    private FriendModule module;
    public FriendAddVigorListener(FriendModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        FriendAddVigorEvent vigorEvent = (FriendAddVigorEvent) event;
        module.innerAddVigorList(vigorEvent.getFriendId());
    }
}
