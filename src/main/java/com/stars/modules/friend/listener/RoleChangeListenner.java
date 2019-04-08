package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/6/16.
 */
public class RoleChangeListenner extends AbstractEventListener<FriendModule> {
    public RoleChangeListenner(FriendModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent)event);
    }
}
