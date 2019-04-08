package com.stars.modules.role.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.FriendGetVigorEvent;

/**
 * Created by wuyuxing on 2016/11/15.
 */
public class FriendGetVigorListener implements EventListener {

    RoleModule roleModule;

    public FriendGetVigorListener(RoleModule roleModule) {
        this.roleModule = roleModule;
    }

    @Override
    public void onEvent(Event event) {
        FriendGetVigorEvent friendEvent = (FriendGetVigorEvent) event;
        roleModule.innerAddFriendVigor(friendEvent.getVigor());
    }
}
