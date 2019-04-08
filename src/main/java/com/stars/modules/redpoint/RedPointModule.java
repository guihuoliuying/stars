package com.stars.modules.redpoint;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;

import java.util.Map;

/**
 * Created by daiyaorong on 2016/11/16.
 */
public class RedPointModule extends AbstractModule {

    public RedPointModule(long id, Player self, EventDispatcher eventDispatcher,
                          Map<String, Module> moduleMap) {
        super("红点", id, self, eventDispatcher, moduleMap);
    }

}