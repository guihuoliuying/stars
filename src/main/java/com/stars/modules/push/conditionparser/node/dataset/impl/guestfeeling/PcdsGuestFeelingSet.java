package com.stars.modules.push.conditionparser.node.dataset.impl.guestfeeling;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsGuestFeelingSet extends PushCondDataSet {

    private Iterator<Integer> iterator;

    public PcdsGuestFeelingSet() {
    }

    public PcdsGuestFeelingSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        GuestModule guestModule = module(MConst.Guest);
        iterator = guestModule.getFeelingSet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsGuestFeeling(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsGuestFeeling.fieldSet();
    }
}
