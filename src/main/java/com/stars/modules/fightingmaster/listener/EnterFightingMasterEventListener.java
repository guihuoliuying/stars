package com.stars.modules.fightingmaster.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.fightingmaster.FightingMasterModule;
import com.stars.services.fightingmaster.event.EnterFightingMasterEvent;

/**
 * Created by zhouyaohui on 2016/11/16.
 */
public class EnterFightingMasterEventListener extends AbstractEventListener {

    public EnterFightingMasterEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        EnterFightingMasterEvent enterEvent = (EnterFightingMasterEvent) event;
        FightingMasterModule fm = (FightingMasterModule) module();
        fm.enterCallback(enterEvent.isSuccess(), enterEvent.getEnterPacket());
    }
}
