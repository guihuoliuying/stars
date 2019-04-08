package com.stars.modules.welfareaccount.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.welfareaccount.WelfareAccountModule;

/**
 * Created by huwenjun on 2017/4/14.
 */
public class VirtualMoneyListenner extends AbstractEventListener<WelfareAccountModule> {
    WelfareAccountModule welfareAccountModule;

    public VirtualMoneyListenner(WelfareAccountModule module) {
        super(module);
        this.welfareAccountModule = module;
    }

    @Override
    public void onEvent(Event event) {
        this.welfareAccountModule.queryAccountMoneyInner();
    }
}
