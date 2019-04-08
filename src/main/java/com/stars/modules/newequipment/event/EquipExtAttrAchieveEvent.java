package com.stars.modules.newequipment.event;

import com.stars.core.event.Event;
import com.stars.modules.tool.userdata.ExtraAttrVo;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/20.
 */
public class EquipExtAttrAchieveEvent extends Event {
    private Map<Byte,Map<Byte,ExtraAttrVo>> extAttrMap;

    public EquipExtAttrAchieveEvent(Map<Byte, Map<Byte, ExtraAttrVo>> extAttrMap) {
        this.extAttrMap = extAttrMap;
    }

    public Map<Byte, Map<Byte, ExtraAttrVo>> getExtAttrMap() {
        return extAttrMap;
    }
}
