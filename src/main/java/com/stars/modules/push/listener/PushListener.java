package com.stars.modules.push.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.push.PushModule;

/**
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PushListener extends AbstractEventListener<PushModule> {

    public PushListener(PushModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
