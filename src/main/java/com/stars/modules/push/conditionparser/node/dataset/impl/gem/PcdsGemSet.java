package com.stars.modules.push.conditionparser.node.dataset.impl.gem;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.prodata.GemLevelVo;
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
public class PcdsGemSet extends PushCondDataSet {

    private Iterator<RoleToolRow> unembeddedIterator;
    private Iterator<GemLevelVo> embeddedIterator;

    public PcdsGemSet() {
    }

    public PcdsGemSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        ToolModule toolModule = module(MConst.Tool);
        unembeddedIterator = toolModule.getItemMap().values().iterator();

        GemModule gemModule = module(MConst.GEM);
        embeddedIterator = gemModule.getEquipmentGemList().iterator();
    }

    @Override
    public boolean hasNext() {
        return unembeddedIterator.hasNext() || embeddedIterator.hasNext();
    }

    @Override
    public PushCondData next() {
        if (unembeddedIterator.hasNext())
            return new PcdsGem(unembeddedIterator.next());
        else
            return new PcdsGem(embeddedIterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsGem.fieldSet();
    }
}
