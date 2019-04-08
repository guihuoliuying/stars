package com.stars.modules.push.conditionparser.node.dataset.impl.baby;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-08-31.
 */
public class PcdsBabyFashion implements PushCondData {
    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }
    private int fashionId;

    public PcdsBabyFashion(int fashionId) {
        this.fashionId = fashionId;
    }

    @Override
    public long getField(String name) {
        switch (name){
            case "id":
                return fashionId;
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
