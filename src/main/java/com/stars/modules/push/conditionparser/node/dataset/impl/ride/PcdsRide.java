package com.stars.modules.push.conditionparser.node.dataset.impl.ride;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.ride.RideManager;
import com.stars.modules.ride.userdata.RoleRidePo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdsRide implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "type", "awakelevel"));
    }

    private RoleRidePo po;

    public PcdsRide(RoleRidePo po) {
        this.po = po;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return po.getRideId();
            case "type": return RideManager.getRideInfoVo(po.getRideId()).getSkintype();
            case "awakelevel": return po.getAwakeLevel();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    @Override
    public long getOverlayCount() {
        return 0;
    }

    @Override
    public boolean isInvalid() {
        return !po.isOwned(); // 过滤非活跃的
    }
}
