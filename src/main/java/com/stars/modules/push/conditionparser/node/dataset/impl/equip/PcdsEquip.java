package com.stars.modules.push.conditionparser.node.dataset.impl.equip;

import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.tool.ToolManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsEquip implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "lv", "quality", "type", "job"));
    }

    private EquipmentVo vo;

    public PcdsEquip(EquipmentVo vo) {
        this.vo = vo;
    }

    @Override
    public long getField(String name) {
        if (vo == null) {
            return -1;
        }
        switch (name) {
            case "id": return vo.getEquipId();
            case "lv": return vo.getEquipLevel();
            case "quality": return ToolManager.getItemVo(vo.getEquipId()).getColor();
            case "type": return vo.getType();
            case "job": return vo.getJob();
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
