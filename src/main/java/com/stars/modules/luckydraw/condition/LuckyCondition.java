package com.stars.modules.luckydraw.condition;

/**
 * Created by huwenjun on 2017/8/10.
 */
public abstract class LuckyCondition implements ILuckyCondition {
    private int time;

    public LuckyCondition(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
