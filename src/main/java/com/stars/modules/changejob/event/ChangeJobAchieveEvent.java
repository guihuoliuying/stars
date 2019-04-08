package com.stars.modules.changejob.event;

import com.stars.core.event.Event;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class ChangeJobAchieveEvent extends Event {
    private int jobId;

    public int getJobId() {
        return jobId;
    }

    public ChangeJobAchieveEvent(int jobId) {
        this.jobId = jobId;
    }
}
