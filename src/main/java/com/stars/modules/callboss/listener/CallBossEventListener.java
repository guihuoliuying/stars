package com.stars.modules.callboss.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.callboss.CallBossModule;
import com.stars.modules.callboss.event.CallBossStatusChangeEvent;

public class CallBossEventListener extends AbstractEventListener<Module> {
	
	public CallBossEventListener(Module m){
		super(m);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof CallBossStatusChangeEvent) {
			CallBossModule callBossModule = (CallBossModule)module();
			callBossModule.callBossStatusChange();
		}
	}

}
