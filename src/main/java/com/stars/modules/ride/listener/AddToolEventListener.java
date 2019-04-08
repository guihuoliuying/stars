package com.stars.modules.ride.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.ride.RideModule;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class AddToolEventListener implements EventListener {
    private RideModule module;
    public AddToolEventListener(RideModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        module.addList();
    }
}
