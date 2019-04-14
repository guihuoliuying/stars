package com.stars.modules.base.condition.dataset.dungeonpassed;

import com.stars.core.expr.node.dataset.ExprData;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.base.condition.dataset.BaseExprDataSet;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.userdata.RoleDungeon;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class PcdsDungeonPassedSet extends BaseExprDataSet {

    private Iterator<RoleDungeon> iterator;

    public PcdsDungeonPassedSet() {
    }

    public PcdsDungeonPassedSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        iterator = ((DungeonModule) module(MConst.Dungeon)).getRolePassDungeonMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public ExprData next() {
        return new PcdsDungeonPassed(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsDungeonPassed.fieldSet();
    }
}
