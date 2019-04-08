package com.stars.modules.countDown;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * @author zhanghaizhen
 * 2017-06-29
 */
public class CountDownModuleFactory extends AbstractModuleFactory<CountDownModule> {

	public CountDownModuleFactory() {
		super(new CountDownPacketSet());
	}

	@Override
	public CountDownModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		// TODO Auto-generated method stub
		return new CountDownModule(id, self, eventDispatcher, map);
	}

	@Override
	public void loadProductData() throws Exception {

	}

}
