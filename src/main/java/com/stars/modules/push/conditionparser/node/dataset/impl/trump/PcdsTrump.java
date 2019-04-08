package com.stars.modules.push.conditionparser.node.dataset.impl.trump;

import com.stars.modules.mind.userdata.RoleMind;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.trump.userdata.RoleTrumpRow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/7/21.
 */
public class PcdsTrump implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "level", "mind"));
    }

    private RoleTrumpRow trumpPo;
    private RoleMind mindPo;

    public PcdsTrump(RoleTrumpRow trumpPo, RoleMind mindPo) {
        this.trumpPo = trumpPo;
        this.mindPo = mindPo;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return trumpPo.getTrumpId();
            case "level": return trumpPo.getLevel() + 1;
            case "mind": return mindPo != null ? mindPo.getMindLevel() : 0;
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
