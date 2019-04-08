package com.stars.services.marry.event;

import com.stars.core.event.Event;
import com.stars.services.marry.userdata.Marry;

/**
 * Created by zhouyaohui on 2017/1/20.
 */
public class MarryEvent extends Event {

    private Marry marry;

    public MarryEvent(){}

    public Marry getMarry() {
        return marry;
    }

    public void setMarry(Marry marry) {
        this.marry = marry;
    }
}
