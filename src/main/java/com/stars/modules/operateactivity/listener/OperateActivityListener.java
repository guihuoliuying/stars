package com.stars.modules.operateactivity.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class OperateActivityListener extends AbstractEventListener<Module> {
	
	public OperateActivityListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		OperateActivityModule operateActivityModule = (OperateActivityModule) this.module();
        if (event instanceof RoleLevelUpEvent) {
        	operateActivityModule.handleRoleLevelUp();
		}else if (event instanceof ForeShowChangeEvent) {
			operateActivityModule.handleForeShowChange();
		}else if (event instanceof OperateActivityEvent) {
			OperateActivityEvent opEvent = (OperateActivityEvent)event;
			operateActivityModule.handleOperateActivityEvent(opEvent);
		}else if (event instanceof OperateActivityFlowEvent) {
			OperateActivityFlowEvent opfEvent = (OperateActivityFlowEvent)event;
			operateActivityModule.handleOperateActivityFlowEvent(opfEvent);
		}
	}

}
