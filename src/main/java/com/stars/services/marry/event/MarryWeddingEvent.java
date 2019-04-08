package com.stars.services.marry.event;

import com.stars.core.event.Event;
import com.stars.services.marry.userdata.MarryWedding;

/**
 * Created by zhouyaohui on 2017/1/18.
 */
public class MarryWeddingEvent extends Event {

    private MarryWedding wedding;

    public MarryWeddingEvent() {}

    public MarryWedding getWedding() {
        return wedding;
    }

    public void setWedding(MarryWedding wedding) {
        this.wedding = wedding;
    }
}
