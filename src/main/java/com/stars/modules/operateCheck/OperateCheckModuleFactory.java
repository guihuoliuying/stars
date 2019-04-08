package com.stars.modules.operateCheck;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

public class OperateCheckModuleFactory extends AbstractModuleFactory<OperateCheckModule> {

	public OperateCheckModuleFactory() {
		super(null);
	}
	
	@Override
	public OperateCheckModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		return new OperateCheckModule(id, self, eventDispatcher, map);
	}

}
