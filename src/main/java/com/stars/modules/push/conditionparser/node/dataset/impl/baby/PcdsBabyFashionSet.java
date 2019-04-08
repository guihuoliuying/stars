package com.stars.modules.push.conditionparser.node.dataset.impl.baby;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.baby.BabyModule;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-08-31.
 */
public class PcdsBabyFashionSet extends PushCondDataSet {
    private Iterator<Integer> iterator;

    public PcdsBabyFashionSet() {
        super();
    }

    public PcdsBabyFashionSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        BabyModule babyModule = (BabyModule) moduleMap.get(MConst.Baby);
        this.iterator = babyModule.getRoleBaby().getOwnFashionIdSet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsBabyFashion(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsBabyFashion.fieldSet();
    }
}
