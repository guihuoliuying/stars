package com.stars.modules.role.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.role.RoleModule;

/**
 * Created by huwenjun on 2017/6/16.
 */
public class RoleRenameListenner extends AbstractEventListener<RoleModule> {
    public RoleRenameListenner(RoleModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RoleRenameEvent) {
            module().onRoleRename((RoleRenameEvent) event);
        }
    }
}
