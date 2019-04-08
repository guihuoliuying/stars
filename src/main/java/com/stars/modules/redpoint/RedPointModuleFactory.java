package com.stars.modules.redpoint;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by daiyaorong on 2016/11/16.
 */
public class RedPointModuleFactory extends AbstractModuleFactory<RedPointModule> {

    public RedPointModuleFactory() {
        super(new RedPointPacketSet());
    }

    @Override
    public RedPointModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new RedPointModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }
}
