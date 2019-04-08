package com.stars.modules.camp.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class AddProsperousEvent extends Event {
    private int properous;

    public AddProsperousEvent(int properous) {
        this.properous = properous;
    }

    public int getProperous() {
        return properous;
    }

    public void setProperous(int properous) {
        this.properous = properous;
    }
}
