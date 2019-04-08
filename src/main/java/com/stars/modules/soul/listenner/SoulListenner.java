package com.stars.modules.soul.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.soul.SoulModule;

/**
 * Created by huwenjun on 2017/11/24.
 */
public class SoulListenner extends AbstractEventListener<SoulModule> {
    public SoulListenner(SoulModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
