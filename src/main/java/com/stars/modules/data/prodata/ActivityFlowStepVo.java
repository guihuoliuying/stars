package com.stars.modules.data.prodata;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
public class ActivityFlowStepVo {

    private int activityId;
    private int step;
    private String cronExpr;
    private String desc;

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getCronExpr() {
        return cronExpr;
    }

    public void setCronExpr(String cronExpr) {
        this.cronExpr = cronExpr;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
