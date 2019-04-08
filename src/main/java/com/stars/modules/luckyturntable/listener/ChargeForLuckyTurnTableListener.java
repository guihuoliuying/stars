package com.stars.modules.luckyturntable.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.luckyturntable.LuckyTurnTableModule;
import com.stars.modules.luckyturntable.event.InitLuckyEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.ServiceHelper;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public class ChargeForLuckyTurnTableListener extends AbstractEventListener {
    public ChargeForLuckyTurnTableListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        LuckyTurnTableModule tableModule = (LuckyTurnTableModule) module();
        if (event instanceof VipChargeEvent) {
            VipChargeEvent chargeEvent = (VipChargeEvent) event;
            tableModule.handleChargeEvent(chargeEvent.getMoney());
        }
        if (event instanceof InitLuckyEvent) {
            tableModule.putAllLuckyId();
        }
        if (event instanceof ForeShowChangeEvent) {
            ForeShowChangeEvent changeEvent = (ForeShowChangeEvent) event;
            if (changeEvent.getMap().containsKey(ForeShowConst.LUCKTURNTABLE)) {
                ServiceHelper.luckyTurnTableService().sendMainIcon(tableModule.id(), true);
            }
        }
    }
}
