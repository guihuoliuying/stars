package com.stars.modules.runeDungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.runeDungeon.RuneDungeonModule;
import com.stars.modules.runeDungeon.event.RuneDungeonHelpAwardEvent;

public class RuneDungeonListener extends AbstractEventListener<RuneDungeonModule> {

	public RuneDungeonListener(RuneDungeonModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof RuneDungeonHelpAwardEvent){
			RuneDungeonHelpAwardEvent runeEvent = (RuneDungeonHelpAwardEvent)event;
			module().addHelpRward(runeEvent.getToolMap(), runeEvent.getBeHelpId());
		}
	}

}
