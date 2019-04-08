package com.stars.modules.teamdungeon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teamdungeon.TeamDungeonModule;

/**
 * Created by liuyuheng on 2016/9/26.
 */
public class TeamDungeonFinishListener extends AbstractEventListener<TeamDungeonModule> {
    public TeamDungeonFinishListener(TeamDungeonModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().finishReward(event);
    }
}
