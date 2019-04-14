package com.stars.modules.base.condition.dataset.offlineHour;

import com.stars.core.expr.node.dataset.ExprData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class PcdsOffline implements ExprData {
    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("offlinehour", "level", "fight"));
    }

    private int offlineHour;
    private int level;
    private int fight;

    public PcdsOffline(int offlineHour, int level, int fight) {
        this.offlineHour = offlineHour;
        this.level = level;
        this.fight = fight;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "offlinehour": {
                return offlineHour;
            }
            case "level": {
                return level;
            }
            case "fight": {
                return fight;
            }
        }
        throw new IllegalArgumentException("策划数据配错了，loginid没有这个字段->" + name);
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

    @Override
    public String toString() {
        return "PcdsOffline{" +
                "offlineHour=" + offlineHour +
                ", level=" + level +
                ", fight=" + fight +
                '}';
    }
}
