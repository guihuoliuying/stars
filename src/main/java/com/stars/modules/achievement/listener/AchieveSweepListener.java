package com.stars.modules.achievement.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.achievement.AchievementHandler;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;

/**
 * Created by zhouyaohui on 2016/10/20.
 */
public class AchieveSweepListener extends AbstractEventListener {
    public AchieveSweepListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DailyFuntionEvent sweep = (DailyFuntionEvent) event;
        if (sweep.getDailyId() == DailyManager.DAILYID_SWEEPDUNGEON) {
            AchievementModule achievementModule = (AchievementModule) module();
            achievementModule.triggerCheck(AchievementHandler.TYPE_SWEEP, sweep.getCount());
        }
    }
}
