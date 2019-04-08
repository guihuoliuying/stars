package com.stars.modules.push.conditionparser.node.dataset.impl.fashioncard;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-10-20.
 */
public class PcdsFashionCardSet extends PushCondDataSet {
    private Iterator<Integer> iterator;

    public PcdsFashionCardSet() {
    }

    public PcdsFashionCardSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        FashionCardModule cardModule = (FashionCardModule) moduleMap.get(MConst.FashionCard);
        this.iterator = cardModule.getRoleFashionCard().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsFashionCard(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsFashionCard.fieldSet();
    }
}
