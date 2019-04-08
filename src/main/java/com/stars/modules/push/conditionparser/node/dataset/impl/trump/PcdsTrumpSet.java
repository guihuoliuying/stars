package com.stars.modules.push.conditionparser.node.dataset.impl.trump;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.mind.MindModule;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.trump.TrumpModule;
import com.stars.modules.trump.userdata.RoleTrumpRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/7/21.
 */
public class PcdsTrumpSet extends PushCondDataSet {

    private Iterator<RoleTrumpRow> iterator;

    public PcdsTrumpSet() {
    }

    public PcdsTrumpSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        TrumpModule trumpModule = module(MConst.Trump);
        iterator = trumpModule.getRoleTrumpMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        MindModule mindModule = module(MConst.Mind);
        RoleTrumpRow trumpRow = iterator.next();
        return new PcdsTrump(trumpRow, mindModule.getRoleMind(trumpRow.getTrumpId()*100+1));
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsTrump.fieldSet();
    }
}
