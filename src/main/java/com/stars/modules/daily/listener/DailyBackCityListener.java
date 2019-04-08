package com.stars.modules.daily.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;

/**
 * Created by zhanghaizhen on 2017/7/10.
 */
public class DailyBackCityListener extends AbstractEventListener<Module> {
    public DailyBackCityListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DailyModule dailyModule = (DailyModule)module();
        dailyModule.checkAndSendDailyAward(DailyManager.AWARD_PROMPT_BACK_CITY,true);
    }
}
