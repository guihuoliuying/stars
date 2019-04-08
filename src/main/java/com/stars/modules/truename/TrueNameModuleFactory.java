package com.stars.modules.truename;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.truename.gm.TrueNameGmHandler;

import java.util.Map;

public class TrueNameModuleFactory extends AbstractModuleFactory<TrueNameModule> {

	public TrueNameModuleFactory() {
		super(new TrueNamePacketSet());
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		GmManager.reg("trueNameGm", new TrueNameGmHandler());
	}
	
	@Override
	public TrueNameModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		// TODO Auto-generated method stub
		return new TrueNameModule(id, self, eventDispatcher, map);
	}
	
	@Override
	public void loadProductData() throws Exception {
		TrueNameManager.AWARD = DataManager.getCommConfig("certification_reward", 0);
	}

}
