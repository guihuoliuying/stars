package com.stars.modules.foreshow.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.foreshow.ForeShowModule;

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

    }
}
