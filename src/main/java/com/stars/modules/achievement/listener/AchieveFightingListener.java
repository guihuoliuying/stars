package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.role.event.FightScoreAchieveEvent;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveFightingListener extends AbstractEventListener {
    public AchieveFightingListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FightScoreAchieveEvent fightScoreEvent = (FightScoreAchieveEvent) event;
        AchievementModule achievementModule = (AchievementModule) module();
        achievementModule.triggerCheck(AchievementHandler.TYPE_FIGHTING, fightScoreEvent.getNewFightScore());
    }
}
