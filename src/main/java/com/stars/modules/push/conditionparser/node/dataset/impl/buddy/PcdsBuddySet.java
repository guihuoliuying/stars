package com.stars.modules.push.conditionparser.node.dataset.impl.buddy;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsBuddySet extends PushCondDataSet {

    private Iterator<RoleBuddy> iterator;

    public PcdsBuddySet() {
    }

    public PcdsBuddySet(Map<String, Module> moduleMap) {
        super(moduleMap);
        BuddyModule buddyModule = module(MConst.Buddy);
        iterator = buddyModule.getRoleBuddyIterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsBuddy(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsBuddy.fieldSet();
    }
}
