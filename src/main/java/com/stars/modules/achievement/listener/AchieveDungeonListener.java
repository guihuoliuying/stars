package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhouyaohui on 2016/12/19.
 */
public class AchieveDungeonListener extends AbstractEventListener {
    public AchieveDungeonListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule am = (AchievementModule) module();
        am.triggerCheck(AchievementHandler.TYPE_DUNGEON, event);
    }
}
