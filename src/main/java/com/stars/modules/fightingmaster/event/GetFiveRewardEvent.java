package com.stars.modules.fightingmaster.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2017/4/6.
 */
public class GetFiveRewardEvent extends Event {
    private int rewardGroupId;

    public GetFiveRewardEvent(int rewardGroupId) {
        this.rewardGroupId = rewardGroupId;
    }

    public int getRewardGroupId() {
        return rewardGroupId;
    }
}
