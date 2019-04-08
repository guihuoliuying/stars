package com.stars.modules.customerService;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.customerService.listener.CustomerServiceListener;
import com.stars.modules.data.DataManager;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.Map;

public class CustomerServiceModuleFactory extends AbstractModuleFactory<CustomerServiceModule> {

	public CustomerServiceModuleFactory() {
		super(new CustomerServicePacketSet());
	}
	
	@Override
	public CustomerServiceModule newModule(long id, Player self, EventDispatcher eventDispatcher,
                                           Map<String, Module> map) {
		// TODO Auto-generated method stub
		return new CustomerServiceModule(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		CustomerServiceManager.AWARD_DROP_ID = DataManager.getCommConfig("vipcustomer_drop", 0);
	}
	
	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		CustomerServiceListener listener = new CustomerServiceListener((CustomerServiceModule)module);
		
		eventDispatcher.reg(VipChargeEvent.class, listener);
	}

}
