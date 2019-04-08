package com.stars.modules.gm.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.gm.GmModule;
import com.stars.modules.gm.event.GmRedpointEvent;

/**
 * Created by wuyuxing on 2017/3/30.
 */
public class GmRedPointListener extends AbstractEventListener<GmModule> {
    public GmRedPointListener(GmModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GmRedpointEvent) {
            module().signRedPoint();
        }
    }
}
