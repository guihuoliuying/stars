package com.stars.modules.push.conditionparser.node.dataset.impl.chargepref;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.chargepreference.ChargePrefModule;
import com.stars.modules.chargepreference.userdata.RoleChargePrefPo;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsChargePrefSet extends PushCondDataSet {

    private Iterator<RoleChargePrefPo> iterator;

    public PcdsChargePrefSet() {
    }

    public PcdsChargePrefSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        ChargePrefModule module = module(MConst.ChargePref);
        this.iterator = module.prefPoIterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsChargePref(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsChargePref.fieldSet();
    }
}
