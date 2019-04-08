package com.stars.services.marry.event;

import com.stars.core.event.Event;

import java.util.Set;

/**
 * Created by zhouyaohui on 2017/1/17.
 */
public class MarryProfressEvent extends Event {

    private Set<Long> profressList;

    public MarryProfressEvent(Set<Long> profressList) {
        this.profressList = profressList;
    }

    public Set<Long> getProfressList() {
        return profressList;
    }
}
