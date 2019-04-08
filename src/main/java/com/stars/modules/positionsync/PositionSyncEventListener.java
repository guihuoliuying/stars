package com.stars.modules.positionsync;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;

/**
 * Created by zhaowenshuo on 2017/6/28.
 */
public class PositionSyncEventListener extends AbstractEventListener<PositionSyncModule> {

    public PositionSyncEventListener(PositionSyncModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
