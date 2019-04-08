package com.stars.modules.camp.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/7.
 */
public class ActivityFinishEvent extends Event {
    private int activityId;
    private Map<Integer, Integer> reward;

    public ActivityFinishEvent(int activityId, Map<Integer, Integer> reward) {
        this.activityId = activityId;
        this.reward = reward;
    }

    public ActivityFinishEvent() {
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public Map<Integer, Integer> getReward() {
        return reward;
    }

    public void setReward(Map<Integer, Integer> reward) {
        this.reward = reward;
    }
}
