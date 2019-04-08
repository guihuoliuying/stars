package com.stars.modules.push.trigger.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/2.
 */
public class PtOpen extends PushTrigger {

    private String openName;

    @Override
    public void parse(String[] args) {
        this.openName = args[0];
    }

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        if (openName == null) {
            return false;
        }
        ForeShowChangeEvent e = (ForeShowChangeEvent) event;
        if (e.getMap().containsKey(openName)) {
            return true;
        }
        return false;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return ForeShowChangeEvent.class;
    }
}
