package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/6/16.
 */
public class RoleChangeListenner extends AbstractEventListener<MarryModule> {
    public RoleChangeListenner(MarryModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent)event);
    }
}
