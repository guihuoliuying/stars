package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveDaily5v5Listener extends AbstractEventListener {
    public AchieveDaily5v5Listener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule am = (AchievementModule) module();
        am.triggerCheck(AchievementHandler.TYPE_DAILY5V5, event);
    }
}
