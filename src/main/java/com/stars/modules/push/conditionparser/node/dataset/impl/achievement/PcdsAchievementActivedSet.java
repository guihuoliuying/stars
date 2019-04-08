package com.stars.modules.push.conditionparser.node.dataset.impl.achievement;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.achievement.AchievementModule;
import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsAchievementActivedSet extends PushCondDataSet {

    private Iterator<AchievementRow> iterator;

    public PcdsAchievementActivedSet() {
    }

    public PcdsAchievementActivedSet(Map<String, Module> moduleMap) {
        super(moduleMap);
        AchievementModule module = module(MConst.Achievement);
        List<AchievementRow> origin = module.getAchievementRowList();
        Iterator<AchievementRow> originIterator = origin.iterator();
        while (originIterator.hasNext()) {
            if (originIterator.next().getState() == AchievementRow.UNFINISH) {
                originIterator.remove();
            }
        }
        iterator = origin.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public PushCondData next() {
        return new PcdsAchievementActived(iterator.next());
    }

    @Override
    public Set<String> fieldSet() {
        return PcdsAchievementActived.fieldSet();
    }
}
