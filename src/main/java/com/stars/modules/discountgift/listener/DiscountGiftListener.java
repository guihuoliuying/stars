package com.stars.modules.discountgift.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.discountgift.DiscountGiftModule;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushInactivedEvent;
import com.stars.modules.push.event.PushLoginInitEvent;
import com.stars.modules.vip.event.VipChargeEvent;

/**
 * Created by chenxie on 2017/5/26.
 */
public class DiscountGiftListener extends AbstractEventListener<DiscountGiftModule> {


    public DiscountGiftListener(DiscountGiftModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PushLoginInitEvent) {
            module().checkValidity();

        } else if (event instanceof VipChargeEvent) {
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            module().handleChargeEvent(vipChargeEvent.getMoney());

        } else if (event instanceof PushActivedEvent) {
            PushActivedEvent pushActivedEvent = (PushActivedEvent) event;
            module().checkActivedPush();

        } else if (event instanceof PushInactivedEvent) {
            PushInactivedEvent pushInactivedEvent = (PushInactivedEvent) event;
            module().handlePushInActivedEvent(pushInactivedEvent.getPushId());
        }
    }
}
