package com.stars.modules.ride.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-04-01 10:08
 */
public class NewRideEvent extends Event {
    private int rideId;

    public NewRideEvent(int rideId) {
        this.rideId = rideId;
    }

    public int getRideId() {
        return rideId;
    }
}
