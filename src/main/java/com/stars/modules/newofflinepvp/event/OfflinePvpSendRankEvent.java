package com.stars.modules.newofflinepvp.event;

import com.stars.core.event.Event;
import com.stars.services.newofflinepvp.userdata.NewOfflinePvpRankPo;

import java.util.List;

/**
 * Created by chenkeyu on 2017-03-13 16:48
 */
public class OfflinePvpSendRankEvent extends Event {
    private List<NewOfflinePvpRankPo> rankPoList;
    private int onRank;

    public OfflinePvpSendRankEvent() {
    }

    public List<NewOfflinePvpRankPo> getRankPoList() {
        return rankPoList;
    }

    public void setRankPoList(List<NewOfflinePvpRankPo> rankPoList) {
        this.rankPoList = rankPoList;
    }

    public int getOnRank() {
        return onRank;
    }

    public void setOnRank(int onRank) {
        this.onRank = onRank;
    }
}
