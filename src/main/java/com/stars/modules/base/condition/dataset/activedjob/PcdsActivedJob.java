package com.stars.modules.base.condition.dataset.activedjob;

import com.stars.core.expr.node.dataset.ExprData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/6/14.
 */
public class PcdsActivedJob implements ExprData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("jobid"));
    }

    private int jobId;

    public PcdsActivedJob(int jobId) {
        this.jobId = jobId;
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "jobid":
                return jobId;
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
