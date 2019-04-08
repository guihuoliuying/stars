package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class AddReputationEvent extends Event {
    private int reputation;

    public AddReputationEvent(int reputation) {
        this.reputation = reputation;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
}
