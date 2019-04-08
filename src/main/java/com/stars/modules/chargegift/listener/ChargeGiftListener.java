package com.stars.modules.chargegift.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.chargegift.ChargeGiftModule;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.LogUtil;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ChargeGiftListener extends AbstractEventListener<ChargeGiftModule> {

    public ChargeGiftListener(ChargeGiftModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            module().handleChargeEvent(vipChargeEvent.getMoney());
        }
        if (event instanceof ActLoopResetEvent) {
            try {
                module().onDataReq();
                module().onInit(false);
            } catch (Throwable throwable) {
                LogUtil.error("actLoopReset:4006 fail" + module().id(), throwable);
            }
        }
    }

}
