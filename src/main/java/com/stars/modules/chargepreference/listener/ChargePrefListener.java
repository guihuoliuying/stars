package com.stars.modules.chargepreference.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.chargepreference.ChargePrefModule;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ChargePrefListener extends AbstractEventListener<ChargePrefModule> {

    public ChargePrefListener(ChargePrefModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
