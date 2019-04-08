package com.stars.modules.everydaycharge.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.everydaycharge.EverydayChargeModule;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.vip.event.VipChargeEvent;

public class EverydayChargeListener extends AbstractEventListener<EverydayChargeModule> {

	public EverydayChargeListener(EverydayChargeModule module) {
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof LoginSuccessEvent || event instanceof RoleLevelUpEvent) {
    		if (module().isOpenActivity()) {
    			module().matchRoleChargeVo();
    		}
    	} else if (event instanceof VipChargeEvent) {
			module().addLotteryTimes();
		}else if (event instanceof OperateActivityFlowEvent) {
			OperateActivityFlowEvent operateActivityFlowEvent = (OperateActivityFlowEvent)event;
			module().handleOperateActivityFlowEvent(operateActivityFlowEvent);
		}
	}
}
