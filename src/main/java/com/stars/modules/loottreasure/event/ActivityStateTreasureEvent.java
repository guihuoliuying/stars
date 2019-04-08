package com.stars.modules.loottreasure.event;

import com.stars.core.event.Event;
import com.stars.modules.loottreasure.LootTreasureConstant;

/**
 * Created by panzhenfeng on 2016/10/11.
 */
public class ActivityStateTreasureEvent extends Event {
    private LootTreasureConstant.ACTIVITYSEGMENT activitysegment ;

    public ActivityStateTreasureEvent(LootTreasureConstant.ACTIVITYSEGMENT activitysegment) {
        this.activitysegment = activitysegment;
    }


    public LootTreasureConstant.ACTIVITYSEGMENT getActivitysegment() {
        return activitysegment;
    }
}
