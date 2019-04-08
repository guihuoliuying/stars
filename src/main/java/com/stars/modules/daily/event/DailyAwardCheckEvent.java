package com.stars.modules.daily.event;

import com.stars.core.event.Event;

/**
 * Created by zhanghaizhen on 2017/7/17.
 */
public class DailyAwardCheckEvent extends Event {
    private boolean showAward = true;

    public DailyAwardCheckEvent() {
    }

    public DailyAwardCheckEvent(boolean showAward) {
        this.showAward = showAward;
    }

    public boolean isShowAward() {
        return showAward;
    }
}
