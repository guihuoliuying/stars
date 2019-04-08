package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/6/19.
 */
public class RoleChangeListenner extends AbstractEventListener<ArroundPlayerModule> {
    public RoleChangeListenner(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent)event);
    }
}
