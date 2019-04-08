package com.stars.modules.guest.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/6/20.
 */
public class RoleRenameListenner extends AbstractEventListener<GuestModule> {
    public RoleRenameListenner(GuestModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent)event);
    }
}
