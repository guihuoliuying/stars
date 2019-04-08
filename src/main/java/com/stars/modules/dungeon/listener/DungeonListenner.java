package com.stars.modules.dungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dungeon.DungeonModule;

/**
 * Created by huwenjun on 2017/9/30.
 */
public class DungeonListenner extends AbstractEventListener<DungeonModule> {
    public DungeonListenner(DungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
