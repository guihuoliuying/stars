package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.ride.event.RideChangeEvent;

/**
 * Created by zhaowenshuo on 2016/9/30.
 */
public class ArroundPlayerRideChangeListener extends AbstractEventListener<ArroundPlayerModule> {

    public ArroundPlayerRideChangeListener(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().doRideChangeEvent((RideChangeEvent) event);
    }
}
