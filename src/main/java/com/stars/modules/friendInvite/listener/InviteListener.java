package com.stars.modules.friendInvite.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.friendInvite.InviteModule;
import com.stars.modules.friendInvite.event.BindInviteCodeEvent;

/**
 * Created by chenxie on 2017/6/12.
 */
public class InviteListener extends AbstractEventListener<InviteModule> {

    public InviteListener(InviteModule inviteModule) {
        super(inviteModule);
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof BindInviteCodeEvent){
            module().handleInviteEvent((BindInviteCodeEvent)event);
        }
    }

}
