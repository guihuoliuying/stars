package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.newequipment.event.DragonBallChangeEvent;

/**
 * Created by zhanghaizhen on 2017/6/16.
 */
public class ArroundPlayerDragonBallChangeListener  extends AbstractEventListener<ArroundPlayerModule> {
    public ArroundPlayerDragonBallChangeListener(ArroundPlayerModule module) {
        super(module);
    }
    @Override
    public void onEvent(Event event) {
        module().doDragonBallChangeEvent((DragonBallChangeEvent) event);
    }
}
