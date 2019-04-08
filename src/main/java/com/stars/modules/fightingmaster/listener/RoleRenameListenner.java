package com.stars.modules.fightingmaster.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.fightingmaster.FightingMasterModule;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/6/20.
 */
public class RoleRenameListenner extends AbstractEventListener<FightingMasterModule> {
    public RoleRenameListenner(FightingMasterModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent) event);
    }
}
