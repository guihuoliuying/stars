package com.stars.modules.newofflinepvp.event;

import com.stars.core.event.Event;

import java.util.List;

/**
 * Created by chenkeyu on 2017-04-05 14:17
 */
public class OfflinePvpMatchEvent extends Event {
    private List<Long> rankPoList;

    public OfflinePvpMatchEvent(List<Long> rankPoList) {
        this.rankPoList = rankPoList;
    }

    public List<Long> getRankPoList() {
        return rankPoList;
    }
}
