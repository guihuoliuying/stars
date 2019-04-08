package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.name.event.RoleRenameEvent;

/**
 * Created by huwenjun on 2017/7/5.
 */
public class RoleChangeListenner extends AbstractEventListener<CampModule> {
    public RoleChangeListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            module().onChangeJob((ChangeJobEvent) event);
        }
        if (event instanceof RoleRenameEvent) {
            module().onReName((RoleRenameEvent) event);
        }
    }
}
