package com.stars.modules.data;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by Garwah on 2016/3/8.
 */
public class DataModule extends AbstractModule {

    public DataModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("data", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onSyncData(){

    }


}
