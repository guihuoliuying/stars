package com.stars.modules.trump.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.trump.TrumpModule;

/**
 * Created by chenkeyu on 2016/12/2.
 */
public class AddToolEventListener implements EventListener {
    private TrumpModule module;
    public AddToolEventListener(TrumpModule module){
        this.module=module;
    }
    @Override
    public void onEvent(Event event) {
        module.canLevelUp();
    }
}
