package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.achievement.event.JoinActivityEvent;

/**
 * Created by zhouyaohui on 2016/10/19.
 */
public class AchieveActivityListener extends AbstractEventListener {

    public AchieveActivityListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        JoinActivityEvent joinEvent = (JoinActivityEvent) event;
        AchievementModule achievementModule = (AchievementModule) module();
        achievementModule.triggerCheck(AchievementHandler.TYPE_ACTIVITY, joinEvent.getActivity());
    }
}
