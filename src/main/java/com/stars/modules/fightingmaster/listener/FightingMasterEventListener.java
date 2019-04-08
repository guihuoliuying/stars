package com.stars.modules.fightingmaster.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.fightingmaster.FightingMasterModule;
import com.stars.modules.fightingmaster.event.FiveRewardStatusEvent;
import com.stars.modules.fightingmaster.event.GetFiveRewardEvent;

/**
 * Created by zhouyaohui on 2016/11/16.
 */
public class FightingMasterEventListener extends AbstractEventListener {

    public FightingMasterEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
    	FightingMasterModule fightingMasterModule = (FightingMasterModule)module();
    	if (event instanceof FiveRewardStatusEvent) {
			FiveRewardStatusEvent fiveRewardStatusEvent = (FiveRewardStatusEvent)event;
			fightingMasterModule.handleFiveRewardStatusEvent(fiveRewardStatusEvent);
		}else if (event instanceof GetFiveRewardEvent) {
			GetFiveRewardEvent getFiveRewardEvent = (GetFiveRewardEvent)event;
			fightingMasterModule.handleGetFiveRewardEvent(getFiveRewardEvent);
		}
    }
}
