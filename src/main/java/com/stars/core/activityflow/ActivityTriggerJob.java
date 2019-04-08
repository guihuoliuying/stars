package com.stars.core.activityflow;

import com.stars.util.LogUtil;
import org.quartz.*;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
@DisallowConcurrentExecution
public final class ActivityTriggerJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            ActivityFlow flow = (ActivityFlow) map.get("self");
            Object lock = map.get("lock");
            synchronized (lock) {
                flow.onTriggered(context.getTrigger().getJobDataMap().getInt("step"), false);
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }
}