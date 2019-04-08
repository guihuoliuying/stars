package com.stars.modules.teampvpgame.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.teampvpgame.TeamPVPGameModule;
import com.stars.modules.teampvpgame.event.SignUpEvent;

/**
 * Created by liuyuheng on 2016/12/16.
 */
public class TeamPvpGameListener extends AbstractEventListener<TeamPVPGameModule> {
    public TeamPvpGameListener(TeamPVPGameModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (module().isSpecialAccount()) {
            return;
        }
        if (event instanceof SignUpEvent) {
            module().receiveSignUp(((SignUpEvent) event).getSignUpSubmiter());
        }
    }
}
