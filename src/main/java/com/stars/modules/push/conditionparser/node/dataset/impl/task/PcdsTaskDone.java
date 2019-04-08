package com.stars.modules.push.conditionparser.node.dataset.impl.task;

import com.stars.modules.push.conditionparser.node.dataset.PushCondData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/22.
 */
public class PcdsTaskDone implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id"));
    }

    private int taskId;

    public PcdsTaskDone(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return (long) taskId;
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
