package com.stars.modules.push.conditionparser.node.dataset.impl.fashioncard;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-10-20.
 */
public class PcdsFashionCard implements PushCondData {
    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }

    private int fashionCardId;

    public PcdsFashionCard(int fashionCardId) {
        this.fashionCardId = fashionCardId;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id":
                return fashionCardId;
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
