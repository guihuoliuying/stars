package com.stars.modules.push.conditionparser.node.dataset.impl.equipoff;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equip.PcdsEquip;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/4/2.
 */
public class PcdsEquipOffSet extends PushCondDataSet {

    private Iterator<RoleToolRow> unequipedIterator;

    public PcdsEquipOffSet() {
    }

    public PcdsEquipOffSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        ToolModule toolModule = module(MConst.Tool);
        unequipedIterator = toolModule.getEquipToolMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return unequipedIterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsEquip(NewEquipmentManager.getEquipmentVo(unequipedIterator.next().getItemId()));
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsEquip.fieldSet();
    }

}
