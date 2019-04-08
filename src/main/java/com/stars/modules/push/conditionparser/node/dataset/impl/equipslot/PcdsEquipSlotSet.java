package com.stars.modules.push.conditionparser.node.dataset.impl.equipslot;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsEquipSlotSet extends PushCondDataSet {

    private Iterator<RoleEquipment> iterator;

    public PcdsEquipSlotSet() {
    }

    public PcdsEquipSlotSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        NewEquipmentModule equipModule = module(MConst.NewEquipment);
        iterator = equipModule.getRoleEquipMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsEquipSlot(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsEquipSlot.fieldSet();
    }
}
