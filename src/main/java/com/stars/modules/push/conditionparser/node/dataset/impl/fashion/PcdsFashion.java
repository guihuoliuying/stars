package com.stars.modules.push.conditionparser.node.dataset.impl.fashion;

import com.stars.modules.fashion.FashionManager;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhanghaizhen on 2017/8/18.
 */
public class PcdsFashion implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("type"));
    }

    private RoleFashion roleFashion;

    public PcdsFashion(RoleFashion roleFashion) {
        this.roleFashion = roleFashion;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "type": return FashionManager.getFashionVo(roleFashion.getFashionId()).getType();
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
        return !roleFashion.isActive();
    }
}
