package com.stars.modules.fashion.event;

import com.stars.core.event.Event;

/**
 * Created by gaopeidian on 2016/10/10.
 */
public class FashionChangeEvent extends Event {
    private int curFashionId;

    public FashionChangeEvent(int curFashionId) {
        this.curFashionId = curFashionId;
    }

    public int getCurFashionId() {
        return curFashionId;
    }
}
