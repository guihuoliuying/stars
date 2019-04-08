package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.services.marry.event.MarryToolEvent;

/**
 * Created by zhouyaohui on 2016/12/10.
 */
public class MarryToolEventListener extends AbstractEventListener {
    public MarryToolEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryToolEvent mte = (MarryToolEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.handleTool(mte);
    }
}
