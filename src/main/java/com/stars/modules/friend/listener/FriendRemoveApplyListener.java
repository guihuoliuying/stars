package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendRemoveApplyEvent;

/**
 * Created by chenkeyu on 2016/11/25.
 */
public class FriendRemoveApplyListener implements EventListener {
    private FriendModule module;
    public FriendRemoveApplyListener(FriendModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        FriendRemoveApplyEvent friendEvent = (FriendRemoveApplyEvent) event;
        module.innerRemoveApplyList(friendEvent.getFriendId());
    }
}
