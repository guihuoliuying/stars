package com.stars.modules.tecentvideo;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by gaopeidian on 2017/5/8.
 */
public class TencentVideoModuleFactory extends AbstractModuleFactory<TencentVideoModule> {
	public TencentVideoModuleFactory() {
		super(new TencentVideoPacketSet());
	}
	
	@Override
    public TencentVideoModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new TencentVideoModule(id, self, eventDispatcher, moduleMap);
    }
}

