package com.stars.modules.loottreasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.loottreasure.LootTreasureModule;
import com.stars.modules.loottreasure.event.NotifyEnterLootTreasureEvent;

/**
 * Created by panzhenfeng on 2016/11/1.
 */
public class NotifyEnterLootTreasureListener implements EventListener {
    private LootTreasureModule module;

    public NotifyEnterLootTreasureListener(Module module){
        this.module = (LootTreasureModule)module;
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof NotifyEnterLootTreasureEvent){
            NotifyEnterLootTreasureEvent notifyEnterLootTreasureEvent = (NotifyEnterLootTreasureEvent)event;
            this.module.noticeEnteredLootTreasure(notifyEnterLootTreasureEvent.stageId);
        }
    }

}
