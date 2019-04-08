package com.stars.modules.dailyCharge.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dailyCharge.DailyChargeModule;
import com.stars.modules.vip.event.VipChargeEvent;

/**
 * Created by wuyuxing on 2017/3/30.
 */
public class ChargeListenr extends AbstractEventListener<DailyChargeModule> {
    public ChargeListenr(DailyChargeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent)event;
            module().handleChargeEvent(vipChargeEvent.getMoney());
        }
    }
}
