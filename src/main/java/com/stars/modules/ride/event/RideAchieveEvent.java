package com.stars.modules.ride.event;

import com.stars.core.event.Event;

import java.util.Set;

/**
 * Created by wuyuxing on 2017/2/9.
 */
public class RideAchieveEvent extends Event {
    private Set<Integer> rideList;
    private int curLevelId;
    private int ownCount;

    public RideAchieveEvent(Set<Integer> rideList, int curLevelId, int ownCount) {
        this.rideList = rideList;
        this.curLevelId = curLevelId;
        this.ownCount = ownCount;
    }

    public Set<Integer> getRideList() {
        return rideList;
    }

    public int getCurLevelId() {
        return curLevelId;
    }

    public int getOwnCount() {
        return ownCount;
    }
}
