package com.stars.modules.rank.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.rank.RankModule;

/**
 * Created by huwenjun on 2017/6/16.
 */
public class RoleRenameListenner extends AbstractEventListener<RankModule> {
    public RoleRenameListenner(RankModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onRoleRename((RoleRenameEvent) event);
    }
}
