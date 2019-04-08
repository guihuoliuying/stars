package com.stars.modules.newdailycharge.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.newdailycharge.NewDailyChargeModule;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargeListener extends AbstractEventListener {
    public NewDailyChargeListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        NewDailyChargeModule chargeModule = (NewDailyChargeModule) module();
        if (event instanceof VipChargeEvent) {
            VipChargeEvent chargeEvent = (VipChargeEvent) event;
            chargeModule.handleChargeEvent(chargeEvent.getMoney());
        }
        if (event instanceof ActLoopResetEvent) {
            try {
                chargeModule.onDataReq();
            } catch (Throwable throwable) {
                LogUtil.error("actLoopReset:4016 fail:" + chargeModule.id(), throwable);
            }
        }
    }
}
