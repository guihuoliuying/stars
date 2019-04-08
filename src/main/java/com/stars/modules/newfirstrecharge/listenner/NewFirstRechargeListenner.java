package com.stars.modules.newfirstrecharge.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.newfirstrecharge.NewFirstRechargeModule;

/**
 * Created by huwenjun on 2017/9/7.
 */
public class NewFirstRechargeListenner extends AbstractEventListener<NewFirstRechargeModule> {
    public NewFirstRechargeListenner(NewFirstRechargeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
