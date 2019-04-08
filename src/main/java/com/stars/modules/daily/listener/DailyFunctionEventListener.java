package com.stars.modules.daily.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.daily.event.DailyFuntionEvent;

public class DailyFunctionEventListener extends AbstractEventListener<Module> {
	
	public DailyFunctionEventListener(Module m){
		super(m);
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		DailyFuntionEvent de = (DailyFuntionEvent)event;
		DailyModule dm = (DailyModule)module();
		dm.addDailyCount(de.getDailyId(), de.getCount());
	}

}
