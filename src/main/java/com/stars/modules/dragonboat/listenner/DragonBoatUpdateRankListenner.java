package com.stars.modules.dragonboat.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dragonboat.DragonBoatModule;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatUpdateRankListenner extends AbstractEventListener<DragonBoatModule> {
    public DragonBoatUpdateRankListenner(DragonBoatModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().updateOneRank();
    }
}
