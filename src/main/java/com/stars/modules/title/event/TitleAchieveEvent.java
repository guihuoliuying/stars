package com.stars.modules.title.event;

import com.stars.core.event.Event;
import com.stars.modules.title.userdata.RoleTitle;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class TitleAchieveEvent extends Event {
    Map<Integer, RoleTitle> roleTitleMap;

    public Map<Integer, RoleTitle> getRoleTitleMap() {
        return roleTitleMap;
    }

    public TitleAchieveEvent(Map<Integer, RoleTitle> roleTitleMap) {
        this.roleTitleMap = roleTitleMap;
    }
}
