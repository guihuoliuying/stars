package com.stars.modules.ride.event;

import com.stars.core.event.Event;

/**
 * Created by zhaowenshuo on 2016/9/30.
 */
public class RideChangeEvent extends Event {

    private int prevActiveRideId;
    private int currActiveRideId;

    public RideChangeEvent(int prevActiveRideId, int currActiveRideId) {
        this.prevActiveRideId = prevActiveRideId;
        this.currActiveRideId = currActiveRideId;
    }

    public int getPrevActiveRideId() {
        return prevActiveRideId;
    }

    public int getCurrActiveRideId() {
        return currActiveRideId;
    }
}
