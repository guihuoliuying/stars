package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.services.marry.event.MarryLogEvent;

/**
 * Created by zhouyaohui on 2016/12/10.
 */
public class MarryLogListener extends AbstractEventListener {
    public MarryLogListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryLogEvent mte = (MarryLogEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.handleMarryLog(mte);
    }
}
