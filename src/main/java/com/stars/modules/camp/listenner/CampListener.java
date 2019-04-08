package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;

/**
 * Created by zhaowenshuo on 2017/7/11.
 */
public class CampListener extends AbstractEventListener<CampModule> {
    public CampListener(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
