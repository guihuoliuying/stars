package com.stars.modules.camp.event;

import com.stars.core.event.Event;
import com.stars.multiserver.camp.pojo.CampPlayerImageData;

/**
 * Created by huwenjun on 2017/7/3.
 */
public class CampCityChangeEvent extends Event {
    private long roleId;
    private int serverId;
    private int oldCityId;
    private int newCityId;
    private CampPlayerImageData campPlayerImageData;

    public CampCityChangeEvent() {
    }

    public CampCityChangeEvent(long roleId, int serverId, int oldCityId, int newCityId, CampPlayerImageData campPlayerImageData) {
        this.roleId = roleId;
        this.serverId = serverId;
        this.oldCityId = oldCityId;
        this.newCityId = newCityId;
        this.campPlayerImageData = campPlayerImageData;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getOldCityId() {
        return oldCityId;
    }

    public void setOldCityId(int oldCityId) {
        this.oldCityId = oldCityId;
    }

    public int getNewCityId() {
        return newCityId;
    }

    public void setNewCityId(int newCityId) {
        this.newCityId = newCityId;
    }

	public CampPlayerImageData getCampPlayerImageData() {
		return campPlayerImageData;
	}

	public void setCampPlayerImageData(CampPlayerImageData campPlayerImageData) {
		this.campPlayerImageData = campPlayerImageData;
	}
}
