package com.stars.modules.buddy.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.buddy.BuddyModule;

/**
 * Created by huwenjun on 2017/8/31.
 */
public class BuddyListenner extends AbstractEventListener<BuddyModule> {
    public BuddyListenner(BuddyModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
