package com.stars.modules.push.conditionparser.node.dataset.impl.deity;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.role.RoleModule;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsDeityWeaponSet extends PushCondDataSet {

    private int jobId;
    private Iterator<RoleDeityWeapon> iterator;

    public PcdsDeityWeaponSet() {
    }

    public PcdsDeityWeaponSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        DeityWeaponModule deityModule = module(MConst.Deity);
        iterator = deityModule.getRoleDeityWeaponList().iterator();
        RoleModule roleModule = module(MConst.Role);
        jobId = roleModule.getRoleRow().getJobId();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsDeityWeapon(jobId, iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsDeityWeapon.fieldSet();
    }
}
