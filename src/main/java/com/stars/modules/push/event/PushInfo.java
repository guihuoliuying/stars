package com.stars.modules.push.event;

/**
 * Created by zhaowenshuo on 2017/4/5.
 */
public class PushInfo {

    private int activityId;
    private int group;
    private int pushId;

    public PushInfo(int activityId, int group, int pushId) {
        this.activityId = activityId;
        this.group = group;
        this.pushId = pushId;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getGroup() {
        return group;
    }

    public int getPushId() {
        return pushId;
    }
}
