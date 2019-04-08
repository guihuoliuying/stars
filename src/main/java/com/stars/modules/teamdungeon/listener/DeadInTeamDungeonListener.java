package com.stars.modules.teamdungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teamdungeon.TeamDungeonModule;

/**
 * Created by gaopeidian on 2016/11/8.
 */
public class DeadInTeamDungeonListener extends AbstractEventListener<TeamDungeonModule> {
    public DeadInTeamDungeonListener(TeamDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().failReward(event);
    }
}
