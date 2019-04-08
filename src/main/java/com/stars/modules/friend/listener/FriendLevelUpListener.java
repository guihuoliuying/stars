package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class FriendLevelUpListener implements EventListener {

    private FriendModule module;

    public FriendLevelUpListener(FriendModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        RoleLevelUpEvent levelUpEvent = (RoleLevelUpEvent) event;
        ServiceHelper.friendService().updateRoleLevel(module.id(), levelUpEvent.getNewLevel());
    }

}
