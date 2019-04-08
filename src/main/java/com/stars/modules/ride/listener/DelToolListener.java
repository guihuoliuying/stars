package com.stars.modules.ride.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.ride.RideModule;
import com.stars.modules.tool.event.UseToolEvent;

/**
 * Created by chenkeyu on 2016/12/8.
 */
public class DelToolListener implements EventListener {

    private RideModule module;
    public DelToolListener(RideModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        UseToolEvent toolEvent = (UseToolEvent) event;
        module.removeList();
    }
}
