package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhanghaizhen on 2017/8/12.
 */
public class RoleFightChangeListener extends AbstractEventListener {
    public RoleFightChangeListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule am = (AchievementModule) module();
        am.onEvent(event);
    }
}
