package com.stars.modules.newservermoney.event;

import com.stars.core.event.Event;

/**
 * Created by liuyuheng on 2017/1/5.
 */
public class NSMoneyRewardEvent extends Event {
    private byte eventType;//事件类型
    public static final byte TAKE_REWARD=1;
    public static final byte SEND_REWARD_RECORD=2;
    private int moneyTypeId;// 发奖Id
    private long rewardTime;// 发奖时间
    private int curActId;// 当前活动Id

    public NSMoneyRewardEvent(byte eventType,int moneyTypeId, long rewardTime, int curActId) {
        this.eventType=eventType;
        this.moneyTypeId = moneyTypeId;
        this.rewardTime = rewardTime;
        this.curActId = curActId;
    }

    public NSMoneyRewardEvent(byte eventType) {
        this.eventType = eventType;
    }

    public int getMoneyTypeId() {
        return moneyTypeId;
    }

    public long getRewardTime() {
        return rewardTime;
    }

    public int getCurActId() {
        return curActId;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }
}
