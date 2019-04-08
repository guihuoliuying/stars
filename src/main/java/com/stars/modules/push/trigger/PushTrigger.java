package com.stars.modules.push.trigger;

import com.stars.core.event.Event;
import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public abstract class PushTrigger {

    private int pushId;

    public abstract boolean check(Event event, Map<String, Module> moduleMap);

    public abstract Class<? extends Event> eventClass();

    public void parse(String[] args) { }

    private <T> T module(Map<String, Module> moduleMap, String moduleName) {
        return (T) moduleMap.get(moduleName);
    }

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }
}
