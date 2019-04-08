package com.stars.core.activityflow;

import org.quartz.*;

import java.util.Date;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * fixme: 初始化前要先停止之前的定时器，要加上唯一标识
 *
 * 1. many triggers can point to the same job
 * 2. a single trigger can only point to one job
 * 3. the job class must be public!
 * 4. @DisallowConcurrentExecution marks a job class as one that must not have multiple instances executed concurrently
 *    (where instance is based-upon a JobDetail definition - or in other words based upon JobKey)
 *
 * Created by zhaowenshuo on 2016/10/9.
 */
public abstract class ActivityFlow {

    public static final int STEP_START_CHECK = 0;
    public static final int STEP_START = 1;
    public static final int STEP_END = 2;

    private final Object lock = new Object();
    protected Map<Integer, String> configMap;

    private JobKey jobKey = new JobKey("job", getActivityFlowName());

    public final void init(Scheduler scheduler, Map<Integer, String> map) throws Exception {
        synchronized (lock) {
            this.configMap = map;
            scheduler.deleteJob(jobKey);
            JobDetail job = createJobDetail();
            scheduler.addJob(job, true, true);
            if (map != null && map.size() > 0) {
                scheduler.scheduleJob(createStartNowTrigger());
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    scheduler.scheduleJob(createCronTrigger(entry.getKey(), entry.getValue()));
                }
            }
        }
    }

    public final void stop(Scheduler scheduler) throws Exception {
        synchronized (lock) {
            scheduler.deleteJob(jobKey);
        }
    }

    private final JobDetail createJobDetail() {
        JobDataMap map = new JobDataMap();
        map.put("self", this);
        map.put("lock", lock);
        return newJob(ActivityTriggerJob.class)
                .withIdentity(jobKey)
                .setJobData(map)
                .build();
    }

    private final Trigger createCronTrigger(int step, String cron) {
        return newTrigger()
                .forJob(jobKey)
                .usingJobData("step", step)
                .withSchedule(cronSchedule(cron))
                .build();
    }

    private final Trigger createStartNowTrigger() {
        return newTrigger()
                .forJob(jobKey)
                .usingJobData("step", STEP_START_CHECK)
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(1))
                .build();
    }

    public final boolean between(int step1, int step2) {
        return between(System.currentTimeMillis(), step1, step2);
    }

    public final boolean between(long now, int step1, int step2) {
        return ActivityFlowUtil.between(now, configMap.get(step1), configMap.get(step2));
    }

    public final boolean outside(int step1, int step2) {
        return outside(System.currentTimeMillis(), step1, step2);
    }

    public final boolean outside(long now, int step1, int step2) {
        return ActivityFlowUtil.outside(now, configMap.get(step1), configMap.get(step2));
    }
    
    public final long remainder(int step) {
    	return ActivityFlowUtil.remainder(System.currentTimeMillis(), configMap.get(step));
    }

    public final long nextValidTime(int step) {
        try {
            return new CronExpression(configMap.get(step)).getNextValidTimeAfter(new Date()).getTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用来生成Job的Key（重载数据时有效）
     * @return
     */
    public abstract String getActivityFlowName();

    /**
     * step为0时表示服务启动时会触发一次（用作活动状态检查）
     * @param step
     * @param isRedo
     */
    public abstract void onTriggered(int step, boolean isRedo);

}

