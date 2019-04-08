package com.stars.modules.retrievereward.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.retrievereward.RetrieveRewardModule;
import com.stars.modules.retrievereward.event.PreDailyRecordResetEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

public class RetrieveRewardListener extends AbstractEventListener<Module> {
	
	public RetrieveRewardListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		RetrieveRewardModule retrieveRewardModule = (RetrieveRewardModule) this.module();
        if (event instanceof PreDailyRecordResetEvent) {
        	PreDailyRecordResetEvent pEvent = (PreDailyRecordResetEvent)event;
        	retrieveRewardModule.handlePreDailyRecordReset(pEvent);
		}else if (event instanceof OperateActivityEvent) {
			OperateActivityEvent opEvent = (OperateActivityEvent)event;
			retrieveRewardModule.handleOperateActivityEvent(opEvent);
		}else if (event instanceof RoleLevelUpEvent) {
			retrieveRewardModule.handleRoleLevelUp();
		}else if (event instanceof ForeShowChangeEvent) {
			retrieveRewardModule.handleForeShowChange();
		}
	}

}
