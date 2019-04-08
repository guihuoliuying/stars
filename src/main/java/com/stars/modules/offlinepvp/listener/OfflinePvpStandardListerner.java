package com.stars.modules.offlinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.offlinepvp.OfflinePvpModule;
import com.stars.modules.role.event.FightScoreChangeEvent;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class OfflinePvpStandardListerner extends AbstractEventListener<OfflinePvpModule> {
    public OfflinePvpStandardListerner(OfflinePvpModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FightScoreChangeEvent fscEvent = (FightScoreChangeEvent) event;
        module().updateStandard(fscEvent.getRoleLevel(), fscEvent.getNewFightScore());
    }
}
