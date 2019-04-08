package com.stars.modules.searchtreasure.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.searchtreasure.SearchTreasureModule;

/**
 * 通知玩家复活了;
 * Created by panzhenfeng on 2016/10/12.
 */
public class SearchTreasureReviveListener  implements EventListener {

    private SearchTreasureModule module;

    public SearchTreasureReviveListener(Module module){
        this.module = (SearchTreasureModule)module;
    }

    @Override
    public void onEvent(Event event) {
        this.module.selfRevived();
    }

}
