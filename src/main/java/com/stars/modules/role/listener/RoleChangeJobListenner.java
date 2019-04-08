package com.stars.modules.role.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.role.RoleModule;

/**
 * Created by huwenjun on 2017/6/8.
 */
public class RoleChangeJobListenner extends AbstractEventListener<RoleModule> {
    public RoleChangeJobListenner(RoleModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            module().onChangeJob(changeJobEvent.getNewJobId());
        }
    }
}
