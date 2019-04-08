package com.stars.modules.push.conditionparser.node.dataset.impl.achievement;

import com.stars.modules.achievement.userdata.AchievementRow;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsAchievementActived implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }

    private AchievementRow po;

    public PcdsAchievementActived(AchievementRow po) {
        this.po = po;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return po.getAchievementId();
        }
        return 0;
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    @Override
    public long getOverlayCount() {
        return 0;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }
}
