package com.stars.modules.masternotice.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.masternotice.MasterNoticeModule;

public class MasterNoticeListener extends AbstractEventListener<Module> {
	
	public MasterNoticeListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		MasterNoticeModule masterNoticeModule = (MasterNoticeModule) this.module();
		masterNoticeModule.handMasterNoticeEvent(event);
	}

}
