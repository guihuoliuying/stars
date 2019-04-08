package com.stars.modules.push.trigger.impl;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.UseToolEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtUseGold extends PushTrigger {

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        UseToolEvent e = (UseToolEvent) event;
        return e.getItemId() == ToolManager.GOLD || e.getItemId() == ToolManager.BANDGOLD;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return UseToolEvent.class;
    }
}
