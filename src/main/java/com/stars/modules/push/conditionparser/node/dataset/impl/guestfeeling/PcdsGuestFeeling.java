package com.stars.modules.push.conditionparser.node.dataset.impl.guestfeeling;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsGuestFeeling implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }

    private int feelingId;

    public PcdsGuestFeeling(int feelingId) {
        this.feelingId = feelingId;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return feelingId;
        }
        throw new RuntimeException();
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
