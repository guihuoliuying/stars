package com.stars.modules.push.trigger.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.tool.event.UseToolEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtUseItem extends PushTrigger {

    private int itemId;

    @Override
    public void parse(String[] args) {
        this.itemId = Integer.parseInt(args[0]);
    }

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        UseToolEvent e = (UseToolEvent) event;
        return e.getItemId() == itemId;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return UseToolEvent.class;
    }
}
