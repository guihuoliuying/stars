package com.stars.modules.loottreasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.loottreasure.LootTreasureConstant;
import com.stars.modules.loottreasure.LootTreasureModule;
import com.stars.modules.loottreasure.event.ActivityStateTreasureEvent;
import com.stars.modules.loottreasure.packet.ClientLootTreasureInfo;
import com.stars.services.ServiceHelper;

/**
 * Created by panzhenfeng on 2016/10/11.
 */
public class ActivityStateTreasureListener implements EventListener {

    private LootTreasureModule lootTreasureModule;

    public ActivityStateTreasureListener(Module m) {
        lootTreasureModule = (LootTreasureModule) m;
    }

    @Override
    public void onEvent(Event event) {
        ActivityStateTreasureEvent e = (ActivityStateTreasureEvent) event;
        ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_ACTIVITY_NOTICE);
        clientLootTreasureInfo.setStartStamp(ServiceHelper.lootTreasureService().getStartActivityTimeStamp());
        clientLootTreasureInfo.setEndStamp(ServiceHelper.lootTreasureService().getEndActivityTimeStamp());
        clientLootTreasureInfo.setActivitySegment(e.getActivitysegment());
        lootTreasureModule.send(clientLootTreasureInfo);
        if (e.getActivitysegment().equals(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_START)) {
            lootTreasureModule.setInLootTreasure(true);
        }
        if (e.getActivitysegment().equals(LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYSE_END)) {
            lootTreasureModule.setInLootTreasure(false);
        }
    }
}
