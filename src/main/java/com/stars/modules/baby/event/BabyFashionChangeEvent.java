package com.stars.modules.baby.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/8/18.
 */
public class BabyFashionChangeEvent extends Event {
    private int curFashionId;

    public BabyFashionChangeEvent(int curFashionId) {
        this.curFashionId = curFashionId;
    }

    public int getCurFashionId() {
        return curFashionId;
    }

    public void setCurFashionId(int curFashionId) {
        this.curFashionId = curFashionId;
    }
}
