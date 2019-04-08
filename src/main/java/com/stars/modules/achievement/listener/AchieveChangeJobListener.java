package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class AchieveChangeJobListener extends AbstractEventListener {
    public AchieveChangeJobListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule am = (AchievementModule) module();
        am.triggerCheck(AchievementHandler.TYPE_CHANGEJOB, event);
    }
}
