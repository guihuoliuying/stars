package com.stars.modules.levelSpeedUp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.levelSpeedUp.LevelSpeedUpModule;
import com.stars.modules.levelSpeedUp.event.LevelSpeedUpEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class LevelSpeedUpListener extends AbstractEventListener<LevelSpeedUpModule> {

	public LevelSpeedUpListener(LevelSpeedUpModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		if((event instanceof RoleLevelUpEvent) || (event instanceof LevelSpeedUpEvent)){
			module().updateAddtion();
		}
	}

}
