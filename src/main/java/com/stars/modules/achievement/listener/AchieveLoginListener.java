package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.demologin.event.LoginSuccessEvent;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveLoginListener extends AbstractEventListener {
    public AchieveLoginListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        LoginSuccessEvent loginEvent = (LoginSuccessEvent) event;
        AchievementModule achieveModule = (AchievementModule) module();
        achieveModule.triggerCheck(AchievementHandler.TYPE_LOGIN, null);
    }
}
