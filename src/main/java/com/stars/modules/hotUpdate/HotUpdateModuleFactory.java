package com.stars.modules.hotUpdate;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class HotUpdateModuleFactory extends AbstractModuleFactory<HotUpdateModule> {
    public HotUpdateModuleFactory() {
        super(new HotUpdatePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {

    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public HotUpdateModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new HotUpdateModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }
}
