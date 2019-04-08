package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.friend.FriendModule;

/**
 * Created by zhaowenshuo on 2017/6/10.
 */
public class FriendRemoveAllVigorListener extends AbstractEventListener<FriendModule> {

    public FriendRemoveAllVigorListener(FriendModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().innerRemoveAllVigorList();
    }
}
