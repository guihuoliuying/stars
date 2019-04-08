package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.tool.event.AddToolEvent;

/**
 * Created by zhouyaohui on 2016/10/18.
 */
public class AchieveToolListener extends AbstractEventListener {

    public AchieveToolListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AchievementModule achievementModule = (AchievementModule) module();
        AddToolEvent addToolEvent = (AddToolEvent) event;
        achievementModule.triggerCheck(AchievementHandler.TYPE_TOOL, addToolEvent.getToolMap());
    }
}
