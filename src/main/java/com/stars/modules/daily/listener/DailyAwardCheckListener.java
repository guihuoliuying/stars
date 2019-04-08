package com.stars.modules.daily.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.daily.event.DailyAwardCheckEvent;

/**
 * Created by zhanghaizhen on 2017/7/17.
 */
public class DailyAwardCheckListener extends AbstractEventListener<Module> {

    public DailyAwardCheckListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DailyAwardCheckEvent checkEvent = (DailyAwardCheckEvent) event;
        DailyModule dailyModule = (DailyModule)module();
        dailyModule.checkAndSendDailyAward(DailyManager.AWARD_PROMPT_IMMEDIATE,checkEvent.isShowAward());
    }
}
