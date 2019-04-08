package com.stars.modules.push.conditionparser.node.dataset.impl.equipon;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equip.PcdsEquip;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/4/2.
 */
public class PcdsEquipOnSet extends PushCondDataSet {

    private Iterator<RoleEquipment> equipedIterator;

    public PcdsEquipOnSet() {
    }

    public PcdsEquipOnSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        NewEquipmentModule equipModule = module(MConst.NewEquipment);
        equipedIterator = equipModule.getRoleEquipMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return equipedIterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsEquip(NewEquipmentManager.getEquipmentVo(equipedIterator.next().getEquipId()));
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsEquip.fieldSet();
    }
}
