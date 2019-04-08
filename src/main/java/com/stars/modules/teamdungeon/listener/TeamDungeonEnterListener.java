package com.stars.modules.teamdungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teamdungeon.TeamDungeonModule;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class TeamDungeonEnterListener extends AbstractEventListener<TeamDungeonModule> {
    public TeamDungeonEnterListener(TeamDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().enterHandler(event);
    }
}
