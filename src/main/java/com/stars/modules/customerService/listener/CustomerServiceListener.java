package com.stars.modules.customerService.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.customerService.CustomerServiceModule;
import com.stars.modules.vip.event.VipChargeEvent;

public class CustomerServiceListener extends AbstractEventListener<CustomerServiceModule> {

	public CustomerServiceListener(CustomerServiceModule module) {
		super(module);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent)event;
            module().handleChargeEvent(vipChargeEvent.getMoney());
        }
	}

}
