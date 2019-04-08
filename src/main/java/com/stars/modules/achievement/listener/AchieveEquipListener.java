package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;

/**
 * Created by zhouyaohui on 2016/12/19.
 */
public class AchieveEquipListener extends AbstractEventListener {
    public AchieveEquipListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule achievementModule = (AchievementModule) module();
        achievementModule.triggerCheck(AchievementHandler.TYPE_EQUIP, event);
    }
}
