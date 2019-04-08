package com.stars.modules.elitedungeon.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/7.
 */
public class EliteDungeonDropEvent extends Event {
    private Map<String, Integer> dropIds;

    public EliteDungeonDropEvent(Map<String, Integer> dropIds) {
        this.dropIds = dropIds;
    }

    public Map<String, Integer> getDropIds() {
        return dropIds;
    }
}
