package com.stars.modules.role.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.ReduceRoleResourceEvent;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class ReduceRoleResourceListener extends AbstractEventListener<RoleModule> {
    public ReduceRoleResourceListener(RoleModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReduceRoleResourceEvent) {
            module().gmReduceHandler((ReduceRoleResourceEvent) event);
        }
    }
}
