package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/7/7.
 */
public class MissionFinishEvent extends Event {
    private int missionId;

    public MissionFinishEvent() {
    }

    public MissionFinishEvent(int missionId) {
        this.missionId = missionId;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }
}
