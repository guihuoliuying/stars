package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveTrumpListener extends AbstractEventListener {
    public AchieveTrumpListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule achievementModule = (AchievementModule) module();
        achievementModule.triggerCheck(AchievementHandler.TYPE_TRUMP, event);
    }
}
