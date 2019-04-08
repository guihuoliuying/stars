package com.stars.core.activityflow;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.util.log.CoreLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * 活动流程具体控制类
 * Created by zhaowenshuo on 2016/10/9.
 */
public class ActivityFlowExample extends ActivityFlow {

    private boolean isStarted = false;

    @Override
    public String getActivityFlowName() {
        return "activityFlowExample";
    }

    @Override
    public void onTriggered(int step, boolean isRedo) {
        switch (step) {
            case 1:
                if (!isStarted) {
                    isStarted = true;
                } else {
                }
                // 控制图标展示
                break;
            case 2:
                if (isStarted) {
                    // 控制图标展示
                }
                break;
            case 3:
                if (isStarted) {
                    isStarted = false;
                    // 控制图标展示
                }
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        CoreLogger.init("root");
//        SchedulerHelper.initAndStart("./config/jobs/quartz.properties");
        SchedulerHelper.init("./config/jobs/quartz.properties");
        SchedulerHelper.getScheduler().standby();
        // -- 配置数据，利用cron表达式简化对时间的操作（读库）start
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "1 */2 * * * ?");
        map.put(2, "5 */2 * * * ?");
        map.put(3, "10 */2 * * * ?");
        // -- 配置数据（读库）end
        ActivityFlowExample flow = new ActivityFlowExample();
        flow.init(SchedulerHelper.getScheduler(), map); // 初始化
        SchedulerHelper.start();
        SchedulerHelper.getScheduler().resumeAll();
//        CronExpression ce = new CronExpression("");
    }
}
