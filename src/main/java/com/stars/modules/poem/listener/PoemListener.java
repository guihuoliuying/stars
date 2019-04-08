package com.stars.modules.poem.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.poem.PoemModule;
import com.stars.modules.scene.event.PassStageEvent;

public class PoemListener extends AbstractEventListener<Module> {
	
	public PoemListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		PoemModule poemModule = (PoemModule)this.module();
		if (event instanceof PassStageEvent) {
			PassStageEvent passStageEvent = (PassStageEvent)event;
			poemModule.handlePassStageEvent(passStageEvent);
		}
	}

}
