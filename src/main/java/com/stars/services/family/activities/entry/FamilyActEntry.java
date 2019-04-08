package com.stars.services.family.activities.entry;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/10/8.
 */
public class FamilyActEntry {

    private int activityId;
    /* 活动状态 */
    private int flag; // 显示控制位（怎么显示）
    private int deadlineOfCountdown; // 倒计时的时间点（倒数到那个时间点为止）
    private String text;

    public FamilyActEntry(int activityId) {
        this.activityId = activityId;
    }

    public void writeToBuffer(NewByteBuffer buff, int mask) {
        buff.writeInt(activityId); // 活动id
        buff.writeInt(flag & mask); // 显示控制位（怎么显示），参见FamilyConst.ACT_BTN_***
        buff.writeInt(deadlineOfCountdown); // 倒计时的时间点
        buff.writeString(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getDeadlineOfCountdown() {
        return deadlineOfCountdown;
    }

    public void setDeadlineOfCountdown(int deadlineOfCountdown) {
        this.deadlineOfCountdown = deadlineOfCountdown;
    }

    public static void main(String[] args) {
    }
}
