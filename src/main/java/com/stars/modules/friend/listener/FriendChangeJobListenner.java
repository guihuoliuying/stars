package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.friend.FriendModule;
import com.stars.services.ServiceHelper;

/**
 * Created by huwenjun on 2017/6/2.
 */
public class FriendChangeJobListenner extends AbstractEventListener<FriendModule> {
    public FriendChangeJobListenner(FriendModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            ServiceHelper.friendService().updateRoleJob(module().id(), changeJobEvent.getNewJobId());
        }
    }
}
