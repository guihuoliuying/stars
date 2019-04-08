package com.stars.modules.push.trigger.impl.equip;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.modules.newequipment.event.EquipStarChangeEvent;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PtEquipStarLevelUp extends PushTrigger {

    @Override
    public boolean check(Event event, Map<String, Module> moduleMap) {
        return true;
    }

    @Override
    public Class<? extends Event> eventClass() {
        return EquipStarChangeEvent.class;
    }
}
