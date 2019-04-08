package com.stars.modules.fashioncard.event;

import com.stars.core.event.Event;

/**
 * Created by chenkeyu on 2017-10-18.
 */
public class FashionCardEvent extends Event {
    private int curFashionCardId;

    public FashionCardEvent(int curFashionCardId) {
        this.curFashionCardId = curFashionCardId;
    }

    public int getCurFashionCardId() {
        return curFashionCardId;
    }
}
