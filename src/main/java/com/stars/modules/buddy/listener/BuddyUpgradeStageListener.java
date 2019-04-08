package com.stars.modules.buddy.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.buddy.BuddyModule;

/**
 * Created by liuyuheng on 2016/11/24.
 */
public class BuddyUpgradeStageListener implements EventListener {
    private BuddyModule module;
    public BuddyUpgradeStageListener(BuddyModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        module.changeStage();
    }
}
