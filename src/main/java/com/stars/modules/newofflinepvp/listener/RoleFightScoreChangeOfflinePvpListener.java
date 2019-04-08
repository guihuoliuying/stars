package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.role.event.FightScoreChangeEvent;

/**
 * Created by chenkeyu on 2017-03-15 10:29
 */
public class RoleFightScoreChangeOfflinePvpListener implements EventListener {
    private NewOfflinePvpModule module;

    public RoleFightScoreChangeOfflinePvpListener(NewOfflinePvpModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        FightScoreChangeEvent fightScoreChangeEvent = (FightScoreChangeEvent) event;
        module.changeFightScore(fightScoreChangeEvent.getNewFightScore());
    }
}
