package com.stars.modules.trump.event;

import com.stars.core.event.Event;
import com.stars.modules.trump.userdata.RoleTrumpRow;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class TrumpAchieveEvent extends Event {
    private Map<Integer, RoleTrumpRow> roleTrumpMap;

    public Map<Integer, RoleTrumpRow> getRoleTrumpMap() {
        return roleTrumpMap;
    }

    public TrumpAchieveEvent(Map<Integer, RoleTrumpRow> roleTrumpMap) {
        this.roleTrumpMap = roleTrumpMap;
    }
}
