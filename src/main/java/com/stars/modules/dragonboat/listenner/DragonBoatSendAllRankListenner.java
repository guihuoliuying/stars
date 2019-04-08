package com.stars.modules.dragonboat.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dragonboat.DragonBoatModule;
import com.stars.modules.dragonboat.event.DragonBoatHistorySendEvent;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatSendAllRankListenner extends AbstractEventListener<DragonBoatModule> {
    public DragonBoatSendAllRankListenner(DragonBoatModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DragonBoatHistorySendEvent dragonBoatHistorySendEvent = (DragonBoatHistorySendEvent) event;
        module().sendAllHistoryRank(dragonBoatHistorySendEvent.getRankMap());
    }
}
