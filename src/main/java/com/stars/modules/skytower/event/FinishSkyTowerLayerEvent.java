package com.stars.modules.skytower.event;

import com.stars.core.event.Event;

/**
 * Created by panzhenfeng on 2016/10/13.
 */
public class FinishSkyTowerLayerEvent extends Event {
    private long roleId;
    private int layerSerial;

    public FinishSkyTowerLayerEvent(long roleId, int layerSerial){
        this.roleId = roleId;
        this.layerSerial = layerSerial;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getLayerSerial() {
        return layerSerial;
    }

}
