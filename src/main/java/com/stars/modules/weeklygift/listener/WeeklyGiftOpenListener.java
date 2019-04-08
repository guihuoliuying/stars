package com.stars.modules.weeklygift.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.weeklygift.WeeklyGiftModule;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class WeeklyGiftOpenListener extends AbstractEventListener<WeeklyGiftModule> {

    public WeeklyGiftOpenListener(WeeklyGiftModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ForeShowChangeEvent) {
            ForeShowChangeEvent changeEvent = (ForeShowChangeEvent) event;
            if (changeEvent.getMap().containsKey(ForeShowConst.WEEKLYGIFT)) {
                module().initRoleData();
            }
        }
        if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            module().doCharge(vipChargeEvent.getMoney());
        }
    }
}
