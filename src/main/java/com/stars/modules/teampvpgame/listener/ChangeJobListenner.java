package com.stars.modules.teampvpgame.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.teampvpgame.TeamPVPGameModule;

/**
 * Created by huwenjun on 2017/6/2.
 */
public class ChangeJobListenner extends AbstractEventListener<TeamPVPGameModule> {
    public ChangeJobListenner(TeamPVPGameModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent changeJobEvent = (ChangeJobEvent) event;
            module().updateTPGTeamMember();
        }
    }
}
