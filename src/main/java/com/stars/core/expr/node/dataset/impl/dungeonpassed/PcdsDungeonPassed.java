package com.stars.core.expr.node.dataset.impl.dungeonpassed;

import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.modules.dungeon.userdata.RoleDungeon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class PcdsDungeonPassed implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }

    private RoleDungeon po;

    public PcdsDungeonPassed(RoleDungeon po) {
        this.po = po;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return po.getDungeonId();
        }
        return 0;
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
        return false;
    }
}
