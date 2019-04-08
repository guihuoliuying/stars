package com.stars.modules.push.conditionparser.node.dataset.impl.soul;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.soul.SoulModule;
import com.stars.modules.soul.prodata.SoulLevel;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/11/22.
 */
public class PcdsSoulGodSet extends PushCondDataSet {
    private Iterator<SoulLevel> iterator;

    public PcdsSoulGodSet() {
    }

    public PcdsSoulGodSet(Map<String, Module> moduleMap) {
        SoulModule soulModule = (SoulModule) moduleMap.get(MConst.Soul);
        Map<Integer, SoulLevel> soulLevelsMap = soulModule.getSoulLevelsMap();
        iterator = soulLevelsMap.values().iterator();

    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsSoulGod(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsSoulGod.fieldSet();
    }
}
