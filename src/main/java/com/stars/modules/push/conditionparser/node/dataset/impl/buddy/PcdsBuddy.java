package com.stars.modules.push.conditionparser.node.dataset.impl.buddy;

import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsBuddy implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "lv", "arm", "advance", "isfollow", "isfight", "fight"));
    }

    private RoleBuddy buddy;

    public PcdsBuddy(RoleBuddy buddy) {
        this.buddy = buddy;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return buddy.getBuddyId(); // id
            case "lv": return buddy.getLevel(); // 等级
            case "arm": return buddy.getArmLevel(); // 悟性
            case "advance": return buddy.getStageLevel(); // 阶
            case "isfollow": return buddy.getIsFollow(); // 是否跟随
            case "isfight": return buddy.getIsFight(); // 是否出战
            case "fight": return buddy.getFightScore(); // 战力
        }
        throw new IllegalStateException();
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
