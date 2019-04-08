package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.newofflinepvp.event.OfflinePvpMatchEvent;

/**
 * Created by chenkeyu on 2017-04-05 14:20
 */
public class OfflinePvpMatchLitener implements EventListener {
    private NewOfflinePvpModule module;

    public OfflinePvpMatchLitener(NewOfflinePvpModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        OfflinePvpMatchEvent matchEvent = (OfflinePvpMatchEvent) event;
        module.doMatchEvent(matchEvent.getRankPoList());
    }
}
