package com.stars.modules.newserversign.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.newserversign.NewServerSignModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class NewServerSignListener extends AbstractEventListener<Module> {
	
	public NewServerSignListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		NewServerSignModule newServerSignModule = (NewServerSignModule)this.module();
	    if (event instanceof OperateActivityEvent) {
			OperateActivityEvent opEvent = (OperateActivityEvent)event;
			newServerSignModule.handleOperateActivityEvent(opEvent);
		}else if (event instanceof OperateActivityFlowEvent) {
			OperateActivityFlowEvent opFlowEvent = (OperateActivityFlowEvent)event;
			newServerSignModule.handleOperateActivityFlowEvent(opFlowEvent);
		}else if (event instanceof RoleLevelUpEvent) {
			newServerSignModule.handleRoleLevelUp();
		}else if (event instanceof ForeShowChangeEvent) {
			newServerSignModule.handleForeShowChange();
		}
	}

}
