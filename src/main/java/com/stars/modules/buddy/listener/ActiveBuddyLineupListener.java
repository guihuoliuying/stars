package com.stars.modules.buddy.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.buddy.BuddyModule;

/**
 * Created by liuyuheng on 2016/8/10.
 */
public class ActiveBuddyLineupListener extends AbstractEventListener<BuddyModule> {
    public ActiveBuddyLineupListener(BuddyModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().activeLineupHandler();
        module().changeExp();
        module().changeStage();
        module().changeLineUp();
    }
}
