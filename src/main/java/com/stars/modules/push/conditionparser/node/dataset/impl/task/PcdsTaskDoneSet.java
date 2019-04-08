package com.stars.modules.push.conditionparser.node.dataset.impl.task;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.task.TaskModule;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsTaskDoneSet extends PushCondDataSet {

    private Iterator<Integer> iterator;

    public PcdsTaskDoneSet() {
    }

    public PcdsTaskDoneSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        TaskModule module = module(MConst.Task);
        this.iterator = module.getDoneTask().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsTaskDone(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsTaskDone.fieldSet();
    }
}
