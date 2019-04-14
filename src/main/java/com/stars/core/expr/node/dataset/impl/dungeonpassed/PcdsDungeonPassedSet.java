package com.stars.core.expr.node.dataset.impl.dungeonpassed;

import com.stars.core.expr.node.dataset.PushCondData;
import com.stars.core.expr.node.dataset.PushCondDataSet;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.userdata.RoleDungeon;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/16.
 */
public class PcdsDungeonPassedSet extends PushCondDataSet {

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
    public PushCondData next() {
        return new PcdsDungeonPassed(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsDungeonPassed.fieldSet();
    }
}
