package com.stars.modules.book.event;

import com.stars.core.event.Event;
import com.stars.modules.book.userdata.RoleBookUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/8.
 */
public class BookAchieveEvent extends Event {
    Map<Integer, RoleBookUtil> roleBookUtilMap;

    public Map<Integer, RoleBookUtil> getRoleBookUtilMap() {
        return roleBookUtilMap;
    }
    public BookAchieveEvent(Map<Integer, RoleBookUtil> roleBookUtilMap) {
        this.roleBookUtilMap = roleBookUtilMap;
    }

}
