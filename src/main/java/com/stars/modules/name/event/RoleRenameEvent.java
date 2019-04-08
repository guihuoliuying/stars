package com.stars.modules.name.event;

import com.stars.core.event.Event;

/**
 * Created by huwenjun on 2017/6/14.
 */
public class RoleRenameEvent extends Event {
    private String newName;

    public RoleRenameEvent(String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
