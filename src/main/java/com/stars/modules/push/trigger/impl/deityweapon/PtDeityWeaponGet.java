package com.stars.modules.push.trigger.impl.deityweapon;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.deityweapon.event.ActiveDeityWeaponAchieveEvent;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtDeityWeaponGet extends PushTrigger {

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        return true;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return ActiveDeityWeaponAchieveEvent.class;
    }
}
