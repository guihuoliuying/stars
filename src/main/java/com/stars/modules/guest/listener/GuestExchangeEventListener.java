package com.stars.modules.guest.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.guest.GuestModule;
import com.stars.services.guest.GuestExchangeEvent;

/**
 * Created by zhouyaohui on 2017/1/11.
 */
public class GuestExchangeEventListener extends AbstractEventListener {

    public GuestExchangeEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        GuestModule guestModule = (GuestModule) module();
        GuestExchangeEvent gee = (GuestExchangeEvent) event;
        guestModule.giveCallback(gee.isResult(), gee.getItemId());
    }
}
