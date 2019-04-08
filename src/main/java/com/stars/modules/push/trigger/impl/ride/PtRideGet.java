package com.stars.modules.push.trigger.impl.ride;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.ride.event.RideLevelUpEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtRideGet extends PushTrigger {
    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        RideLevelUpEvent e = (RideLevelUpEvent) event;
        return e.getPrevLevelId() == 0;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return RideLevelUpEvent.class;
    }
}
