package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class EliteDungonAchieveEvent extends Event {
    public int eliteDungonId;

    public EliteDungonAchieveEvent(int eliteDungonId) {
        this.eliteDungonId = eliteDungonId;
    }

    public int getEliteDungonId() {

        return eliteDungonId;
    }
}
