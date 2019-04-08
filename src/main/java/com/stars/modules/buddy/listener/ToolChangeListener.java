package com.stars.modules.buddy.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.buddy.BuddyModule;

/**
 * Created by chenkeyu on 2016/12/10.
 */
public class ToolChangeListener implements EventListener {
    private BuddyModule module;
    public ToolChangeListener(BuddyModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        module.changeActivite();
        module.changeExp();
        module.changeStage();
    }
}
