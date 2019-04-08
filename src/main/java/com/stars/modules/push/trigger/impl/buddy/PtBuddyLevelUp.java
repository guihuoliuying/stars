package com.stars.modules.push.trigger.impl.buddy;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtBuddyLevelUp extends PushTrigger {
    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        BuddyUpgradeEvent e = (BuddyUpgradeEvent) event;
        return e.getType() == BuddyUpgradeEvent.LEVELUP;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return BuddyUpgradeEvent.class;
    }
}
