package com.stars.modules.teamdungeon.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/22.
 */
public class TeamDungeonDropEvent extends Event {
    private Map<String, Integer> dropIds;

    public TeamDungeonDropEvent(Map<String, Integer> dropIds) {
        this.dropIds = dropIds;
    }

    public Map<String, Integer> getDropIds() {
        return dropIds;
    }
}
