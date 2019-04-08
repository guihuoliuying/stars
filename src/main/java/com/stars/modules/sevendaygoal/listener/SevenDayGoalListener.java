package com.stars.modules.sevendaygoal.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.sevendaygoal.SevenDayGoalModule;

public class SevenDayGoalListener extends AbstractEventListener<Module> {
	
	public SevenDayGoalListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		SevenDayGoalModule sevenDayGoalModule = (SevenDayGoalModule) this.module();
		if (event instanceof OperateActivityEvent) {
			OperateActivityEvent opEvent = (OperateActivityEvent)event;
			sevenDayGoalModule.handleOperateActivityEvent(opEvent);
		}else if (event instanceof OperateActivityFlowEvent) {
			OperateActivityFlowEvent opFlowEvent = (OperateActivityFlowEvent)event;
			sevenDayGoalModule.handleOperateActivityFlowEvent(opFlowEvent);
		}else if (event instanceof RoleLevelUpEvent) {
			sevenDayGoalModule.handleRoleLevelUp();
		}else if (event instanceof ForeShowChangeEvent) {
			ForeShowChangeEvent foreShowChangeEvent = (ForeShowChangeEvent)event;
			sevenDayGoalModule.handleForeShowChange(foreShowChangeEvent);
		}else{
			sevenDayGoalModule.handleEvent(event);
		}
	}

}
