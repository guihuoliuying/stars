package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.services.marry.event.MarryAppointSceneCheckEvent;

/**
 * Created by zhouyaohui on 2016/12/10.
 */
public class MarryAppointSceneCheckEventListener extends AbstractEventListener {
    public MarryAppointSceneCheckEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryAppointSceneCheckEvent mte = (MarryAppointSceneCheckEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.handleMarryAppointSceneCheck(mte);
    }
}
