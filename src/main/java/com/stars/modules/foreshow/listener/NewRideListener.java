package com.stars.modules.foreshow.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowManager;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.ride.event.NewRideEvent;

/**
 * Created by chenkeyu on 2016/12/6.
 */
public class NewRideListener implements EventListener {
    private ForeShowModule module;

    public NewRideListener(ForeShowModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        NewRideEvent newRideEvent = (NewRideEvent) event;
        if (ForeShowManager.getIdList(ForeShowConst.RIDE).contains(newRideEvent.getRideId())) {
            module.updateForeShow();
        }
    }
}
