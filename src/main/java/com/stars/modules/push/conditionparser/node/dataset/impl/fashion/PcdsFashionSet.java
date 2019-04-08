package com.stars.modules.push.conditionparser.node.dataset.impl.fashion;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanghaizhen on 2017/8/18.
 */
public class PcdsFashionSet extends PushCondDataSet {

    private Iterator<RoleFashion> iterator;
    public PcdsFashionSet() {
        super();
    }

    public PcdsFashionSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        FashionModule fashionModule = (FashionModule) moduleMap.get(MConst.Fashion);
        iterator = fashionModule.getRoleFashionMap().values().iterator();
    }

    @Override
    protected <T> T module(String name) {
        return super.module(name);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsFashion(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsFashion.fieldSet();
    }
}
