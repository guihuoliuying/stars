package com.stars.modules.dragonboat.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.dragonboat.DragonBoatModule;
import com.stars.modules.dragonboat.event.DragonBoatStatusChangeEvent;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatStatusChangeListenner extends AbstractEventListener<DragonBoatModule> {
    public DragonBoatStatusChangeListenner(DragonBoatModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DragonBoatStatusChangeEvent dragonBoatStatusChangeEvent = (DragonBoatStatusChangeEvent) event;
        LogUtil.info("当前赛龙舟状态——>{}:{}", dragonBoatStatusChangeEvent.getStatus(), dragonBoatStatusChangeEvent.getStageTime());
        module().sendActivityData(true);
    }
}
