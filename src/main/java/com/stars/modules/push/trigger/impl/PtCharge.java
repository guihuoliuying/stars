package com.stars.modules.push.trigger.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PtCharge extends PushTrigger {

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        return true;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return VipChargeEvent.class;
    }
}
