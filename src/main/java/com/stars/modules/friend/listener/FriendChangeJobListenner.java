package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.friend.FriendModule;

/**
 * Created by huwenjun on 2017/6/2.
 */
public class FriendChangeJobListenner extends AbstractEventListener<FriendModule> {
    public FriendChangeJobListenner(FriendModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {

    }
}
