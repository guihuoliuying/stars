package com.stars.modules.push.conditionparser.node.dataset.impl.equipslot;

import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsEquipSlot implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("type", "strength", "star"));
    }

    private RoleEquipment equip;

    public PcdsEquipSlot(RoleEquipment equip) {
        this.equip = equip;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "type": return equip.getType();
            case "strength": return equip.getStrengthLevel();
            case "star": return equip.getStarLevel();
        }
        throw new RuntimeException();
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
