package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.skill.event.SkillLevelAchieveEvent;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveSkillListener extends AbstractEventListener {

    public AchieveSkillListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        SkillLevelAchieveEvent skillEvent = (SkillLevelAchieveEvent) event;
        AchievementModule achievementModule = (AchievementModule) module();
        StringBuilder builder = new StringBuilder();
        builder.append(skillEvent.getSkillId()).append("+")
                .append(skillEvent.getCurLevel());
        achievementModule.triggerCheck(AchievementHandler.TYPE_SKILL, builder.toString());

    }
}
