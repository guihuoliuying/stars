package com.stars.modules.masternotice.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.masternotice.MasterNoticeModule;
import com.stars.modules.masternotice.event.MasterNoticeOnClockEvent;

public class MasterNoticeOnClockListener extends AbstractEventListener<Module> {
	
	public MasterNoticeOnClockListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof MasterNoticeOnClockEvent) {
			MasterNoticeModule masterNoticeModule = (MasterNoticeModule) this.module();
			masterNoticeModule.onClock();
		}
	}

}
