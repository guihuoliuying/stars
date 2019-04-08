package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;
import com.stars.modules.newofflinepvp.event.OfflinePvpSendRankEvent;

/**
 * Created by chenkeyu on 2017-03-13 16:48
 */
public class OfflinePvpSendRanklistener implements EventListener {
    private NewOfflinePvpModule module;

    public OfflinePvpSendRanklistener(NewOfflinePvpModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        OfflinePvpSendRankEvent rankEvent = (OfflinePvpSendRankEvent) event;
        module.sendRankList(rankEvent.getRankPoList(), rankEvent.getOnRank());
    }
}
