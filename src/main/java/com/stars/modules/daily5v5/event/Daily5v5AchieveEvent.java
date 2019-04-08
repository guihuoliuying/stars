package com.stars.modules.daily5v5.event;

import com.stars.core.event.Event;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class Daily5v5AchieveEvent extends Event {
    private int result;

    public Daily5v5AchieveEvent(int result) {
        this.result = result;
    }

    public int getResult() {

        return result;
    }
}
