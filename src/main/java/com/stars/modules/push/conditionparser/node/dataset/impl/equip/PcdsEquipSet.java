package com.stars.modules.push.conditionparser.node.dataset.impl.equip;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsEquipSet extends PushCondDataSet {

    private Iterator<RoleEquipment> equipedIterator;
    private Iterator<RoleToolRow> unequipedIterator;

    public PcdsEquipSet() {
    }

    public PcdsEquipSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        NewEquipmentModule equipModule = module(MConst.NewEquipment);
        equipedIterator = equipModule.getRoleEquipMap().values().iterator();
        ToolModule toolModule = module(MConst.Tool);
        unequipedIterator = toolModule.getEquipToolMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return equipedIterator.hasNext() || unequipedIterator.hasNext();
    }

    @Override
    public PushCondData next() {
        if (equipedIterator.hasNext())
            return new PcdsEquip(NewEquipmentManager.getEquipmentVo(equipedIterator.next().getEquipId()));
        else
            return new PcdsEquip(NewEquipmentManager.getEquipmentVo(unequipedIterator.next().getItemId()));
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsEquip.fieldSet();
    }
}
