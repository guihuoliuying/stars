package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.gem.event.GemEmbedAchievementEvent;


/**
 * Created by zhouyaohui on 2016/10/21.
 */
public class AchieveGemListener extends AbstractEventListener {
    public AchieveGemListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        GemEmbedAchievementEvent gemEvent = (GemEmbedAchievementEvent) event;
        AchievementModule achievementModule = (AchievementModule) module();
        achievementModule.triggerCheck(AchievementHandler.TYPE_GEM, gemEvent.getGemLevelVoList());
    }
}
