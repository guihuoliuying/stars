package com.stars.modules.push.conditionparser.node.dataset.impl.soul;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.soul.prodata.SoulLevel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huwenjun on 2017/11/22.
 */
public class PcdsSoulGod implements PushCondData {
    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("soulgodtype", "soulgodlevel"));
    }

    SoulLevel soulLevel = null;

    public PcdsSoulGod(SoulLevel soulLevel) {
        this.soulLevel = soulLevel;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "soulgodtype": {
                return soulLevel.getSoulGodType();
            }
            case "soulgodlevel": {
                return soulLevel.getSoulGodLevel();

            }
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
