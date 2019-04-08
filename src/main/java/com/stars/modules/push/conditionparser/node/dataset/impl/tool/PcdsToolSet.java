package com.stars.modules.push.conditionparser.node.dataset.impl.tool;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsToolSet extends PushCondDataSet {

    private Iterator<RoleToolRow> iterator;

    public PcdsToolSet() {
    }

    public PcdsToolSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        ToolModule toolModule = module(MConst.Tool);
        iterator = toolModule.getItemMap().values().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsTool(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsTool.fieldSet();
    }
}
