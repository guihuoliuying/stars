package com.stars.modules.loottreasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.loottreasure.LootTreasureModule;
import com.stars.modules.scene.event.RequestExitFightEvent;

/**
 * Created by panzhenfeng on 2016/10/11.
 */
public class BackCityLootTreasureListener implements EventListener {

    private LootTreasureModule module;

    public BackCityLootTreasureListener(Module module){
        this.module = (LootTreasureModule)module;
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof RequestExitFightEvent){
            this.module.noticeBackedCity();
        }
    }

}
