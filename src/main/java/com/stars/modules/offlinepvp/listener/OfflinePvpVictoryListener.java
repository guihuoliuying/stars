package com.stars.modules.offlinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.offlinepvp.OfflinePvpModule;
import com.stars.modules.offlinepvp.event.OfflinePvpVictoryEvent;

/**
 * Created by liuyuheng on 2016/10/12.
 */
public class OfflinePvpVictoryListener extends AbstractEventListener<OfflinePvpModule> {
    public OfflinePvpVictoryListener(OfflinePvpModule module) {
        super(module);
    }



    @Override
    public void onEvent(Event event) {
        module().victory(((OfflinePvpVictoryEvent) event).getEnemyIndex());
        module().canGetPrecious();
    }
}
