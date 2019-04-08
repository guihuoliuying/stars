package com.stars.modules.push.trigger.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.vip.event.VipLevelupEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/1.
 */
public class PtVipLevelUp extends PushTrigger {

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        return true;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return VipLevelupEvent.class;
    }
}
