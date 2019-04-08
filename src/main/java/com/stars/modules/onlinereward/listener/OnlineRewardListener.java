package com.stars.modules.onlinereward.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.onlinereward.OnlineRewardModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class OnlineRewardListener extends AbstractEventListener<Module> {
	
	public OnlineRewardListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		OnlineRewardModule onlineRewardModule = (OnlineRewardModule)this.module();
		if (event instanceof LoginSuccessEvent) {
			onlineRewardModule.handleLoginSuccess();
		}else if (event instanceof OperateActivityEvent) {
			OperateActivityEvent opEvent = (OperateActivityEvent)event;
			onlineRewardModule.handleOperateActivityEvent(opEvent);
		}else if (event instanceof RoleLevelUpEvent) {
			onlineRewardModule.handleRoleLevelUp();
		}else if (event instanceof ForeShowChangeEvent) {
			onlineRewardModule.handleForeShowChange();
		}
	}

}
