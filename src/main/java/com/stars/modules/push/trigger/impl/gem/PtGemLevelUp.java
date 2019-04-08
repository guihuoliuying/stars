package com.stars.modules.push.trigger.impl.gem;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PtGemLevelUp extends PushTrigger {
    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        DailyFuntionEvent e = (DailyFuntionEvent) event;
        return e.getDailyId() == DailyManager.DAILYID_GEM_COMPOSE;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return DailyFuntionEvent.class;
    }
}
