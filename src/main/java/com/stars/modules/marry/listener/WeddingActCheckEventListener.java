package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.services.marry.event.WeddingActCheckEvent;

/**
 * Created by zhouyaohui on 2016/12/10.
 */
public class WeddingActCheckEventListener extends AbstractEventListener {
    public WeddingActCheckEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        WeddingActCheckEvent mte = (WeddingActCheckEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.handleWeddingActEvent(mte);
    }
}
