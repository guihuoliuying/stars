package com.stars.modules.weeklyCharge.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.weeklyCharge.WeeklyChargeModule;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

/**
 * Created by chenxie on 2017/5/5.
 */
public class WeeklyChargeListenr extends AbstractEventListener<WeeklyChargeModule> {

    public WeeklyChargeListenr(WeeklyChargeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            module().handleChargeEvent(vipChargeEvent.getMoney());
        }
        if (event instanceof ActLoopResetEvent) {
            module().onReset();
        }
    }

}
