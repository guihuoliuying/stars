package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.family.event.FamilyAuthAchieveEvent;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveFamilyListener extends AbstractEventListener {
    public AchieveFamilyListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FamilyAuthAchieveEvent ue = (FamilyAuthAchieveEvent) event;
        if (ue.getType() == FamilyAuthAchieveEvent.TYPE_NEW ||
                ue.getType() == FamilyAuthAchieveEvent.TYPE_CREATED) {
            AchievementModule achievementModule = (AchievementModule) module();
            achievementModule.triggerCheck(AchievementHandler.TYPE_FAMILY, null);
        }
    }
}
