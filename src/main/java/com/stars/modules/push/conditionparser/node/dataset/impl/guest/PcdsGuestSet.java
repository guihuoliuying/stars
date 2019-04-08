package com.stars.modules.push.conditionparser.node.dataset.impl.guest;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.guest.userdata.RoleGuest;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdsGuestSet extends PushCondDataSet {

    private Iterator<RoleGuest> iterator;

    public PcdsGuestSet() {
    }

    public PcdsGuestSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        GuestModule guestModule = module(MConst.Guest);
        iterator = guestModule.getGuestMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsGuest(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsGuest.fieldSet();
    }
}
