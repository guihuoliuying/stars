package com.stars.modules.friend.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by zhaowenshuo on 2016/8/27.
 */
public class FriendFightScoreListener extends AbstractEventListener<FriendModule> {

    public FriendFightScoreListener(FriendModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FightScoreChangeEvent changeEvent = (FightScoreChangeEvent) event;
        ServiceHelper.friendService().updateRoleFightScore(module().id(), changeEvent.getNewFightScore());
    }
}
