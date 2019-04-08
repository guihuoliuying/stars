package com.stars.modules.searchtreasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.modules.searchtreasure.SearchTreasureModule;

/**
 * Created by panzhenfeng on 2016/10/12.
 */
public class SearchTreasureEnterCityListener  implements EventListener {

    private SearchTreasureModule module;

    public SearchTreasureEnterCityListener(Module module){
        this.module = (SearchTreasureModule)module;
    }

    @Override
    public void onEvent(Event event) {
        EnterSceneEvent enterSceneEvent = (EnterSceneEvent)event;
        if(enterSceneEvent != null && enterSceneEvent.getLastSceneType() == SceneManager.SCENETYPE_SEARCHTREASURE){
            this.module.backCity();
        }
    }

}

