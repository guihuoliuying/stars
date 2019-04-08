package com.stars.modules.teamdungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teamdungeon.TeamDungeonModule;
import com.stars.modules.teamdungeon.event.BackToCityFromTeamDungeonEvent;

/**
 * Created by gaopeidian on 2016/11/1.
 */
public class BackToCityFromTeamDungeonListener extends AbstractEventListener<TeamDungeonModule> {
    public BackToCityFromTeamDungeonListener(TeamDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        BackToCityFromTeamDungeonEvent btcEvent = (BackToCityFromTeamDungeonEvent) event;
        module().backToCityFromTeamDungeon(btcEvent.getTeamDungeonId());
    }
}
