package com.stars.modules.dragonboat.event;

import com.stars.core.event.Event;
import com.stars.services.rank.userdata.AbstractRankPo;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatHistorySendEvent extends Event {
    private  Map<Long, List<AbstractRankPo>> rankMap;

    public DragonBoatHistorySendEvent(Map<Long, List<AbstractRankPo>> rankMap) {
        this.rankMap = rankMap;
    }

    public Map<Long, List<AbstractRankPo>> getRankMap() {
        return rankMap;
    }

    public void setRankMap(Map<Long, List<AbstractRankPo>> rankMap) {
        this.rankMap = rankMap;
    }
}
