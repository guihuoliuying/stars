package com.stars.modules.teamdungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teamdungeon.TeamDungeonModule;
import com.stars.modules.teamdungeon.event.TeamDungeonDropEvent;

/**
 * Created by liuyuheng on 2016/9/22.
 */
public class TeamDungeonDropListener extends AbstractEventListener<TeamDungeonModule> {
    public TeamDungeonDropListener(TeamDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        TeamDungeonDropEvent tddEvent = (TeamDungeonDropEvent) event;
        module().addMonsterDrop(tddEvent.getDropIds());
    }
}
