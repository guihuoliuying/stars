package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;

/**
 * Created by huwenjun on 2017/6/2.
 */
public class RoleChangeListenner extends AbstractEventListener<NewOfflinePvpModule> {
    public RoleChangeListenner(NewOfflinePvpModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            module().changeRoleJob(changeJobEvent.getNewJobId());
        }
        if(event instanceof RoleRenameEvent){
            RoleRenameEvent roleRenameEvent = (RoleRenameEvent) event;
            module().onRoleReName(roleRenameEvent.getNewName());
        }
    }
}
