package com.stars.modules.dungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dungeon.DungeonModule;

/**
 * Created by liuyuheng on 2016/7/19.
 */
public class ActiveDungeonListener extends AbstractEventListener<DungeonModule> {
    public ActiveDungeonListener(DungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().activeDungeonHandler();
    }
}
