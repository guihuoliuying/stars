package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.event.FriendApplyAddEvent;

/**
 * Created by chenkeyu on 2016/11/25.
 */
public class FriendAddApplyListener implements EventListener {
    private FriendModule module;
    public FriendAddApplyListener(FriendModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        FriendApplyAddEvent friendEvent = (FriendApplyAddEvent) event;
        module.innerAddApplyList(friendEvent.getFriendId());
    }
}
