package com.stars.modules.dragonboat.define;

/**
 * Created by huwenjun on 2017/5/10.
 */
public class StepTime {
    /**
     * 当天
     */
    public static final Integer IS_TODAY = 0;
    /**
     * 明天
     */
    public static final Integer IS_TOMORROW = 1;
    /**
     * 结束
     */
    public static final Integer IS_END = -1;
    private Integer status;
    private Long timestamp;
    private Integer step;
    private Long lastTime;
    private Long activityKey;

    public StepTime(Integer status, Long timestamp, Integer step, Long lastTime, Long activityKey) {
        this.status = status;
        this.timestamp = timestamp;
        this.step = step;
        this.lastTime = lastTime;
        this.activityKey = activityKey;
    }
    public StepTime(Integer status, Long timestamp, Integer step, Long lastTime) {
        this.status = status;
        this.timestamp = timestamp;
        this.step = step;
        this.lastTime = lastTime;
    }
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Long getActivityKey() {
        return activityKey;
    }

    public void setActivityKey(Long activityKey) {
        this.activityKey = activityKey;
    }
}
