package com.stars.modules.push.conditionparser.node.dataset.impl.ride;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.ride.RideModule;
import com.stars.modules.ride.userdata.RoleRidePo;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcdsRideSet extends PushCondDataSet {

    private Iterator<RoleRidePo> iterator;

    public PcdsRideSet() {
    }

    public PcdsRideSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        RideModule rideModule = module(MConst.Ride);
        iterator = rideModule.getRidePoMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsRide(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsRide.fieldSet();
    }
}
