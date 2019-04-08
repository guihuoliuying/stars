package com.stars.modules.buddy.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.buddy.BuddyModule;

/**
 * Created by chenkeyu on 2017/2/14 17:03
 */
public class ForeShowChangeListener implements EventListener {

    private BuddyModule buddyModule;

    public ForeShowChangeListener(BuddyModule buddyModule) {
        this.buddyModule = buddyModule;
    }

    @Override
    public void onEvent(Event event) {
        buddyModule.changeExp();
        buddyModule.changeStage();
        buddyModule.changeLineUp();
    }
}
