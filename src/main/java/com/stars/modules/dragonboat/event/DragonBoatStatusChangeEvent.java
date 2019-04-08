package com.stars.modules.dragonboat.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class DragonBoatStatusChangeEvent extends Event {
    private Integer status;
    private Long stageTime;

    public DragonBoatStatusChangeEvent() {
    }

    public DragonBoatStatusChangeEvent(Integer status, Long stageTime) {
        this.status = status;
        this.stageTime = stageTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getStageTime() {
        return stageTime;
    }

    public void setStageTime(Long stageTime) {
        this.stageTime = stageTime;
    }
}
