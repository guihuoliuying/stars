package com.stars.core.schedule;

import com.stars.core.persist.TriggerAutoSavingTask;
import com.stars.ClearAccountTask;
import com.stars.ExcutorKey;
import com.stars.util.LogUtil;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerManager {

    private static AtomicInteger ExecutorCounter = new AtomicInteger(0);        //线程池计数器
    private static AtomicInteger SchedulerCounter = new AtomicInteger(0);        //线程计数器

    public static int scheduledCorePoolSize = 10;        //默认定时线程池大小
    public static ScheduledExecutorService scheduler;    //默认定时线程池

    private static Map<String, ScheduledExecutorService> SCHEDULE_THREAD_POOL;    //定时线程池列表

    public static void init(int poolSize) {
        SCHEDULE_THREAD_POOL = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(poolSize);
    }

    public static void init() {
        SCHEDULE_THREAD_POOL = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * 线程池初始化
     */
    public static void initScheduler() {
        scheduleAtFixedRateIndependent(ExcutorKey.SaveDB, new TriggerAutoSavingTask(), 10, 1, TimeUnit.SECONDS);
        scheduleAtFixedRateIndependent(ExcutorKey.ClearAccount, new ClearAccountTask(), 10, 30, TimeUnit.SECONDS);
    }

    /**
     * 新增定时任务
     * 默认线程池
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        int curCount = SchedulerCounter.incrementAndGet();
        LogUtil.info("SchedulerManager|新增定时任务|当前定时任务数量为:" + curCount);
        return scheduler.scheduleAtFixedRate(new RunnableTask(command), initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRateIndependent(int key, Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(Integer.toString(key));
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(1);
            SCHEDULE_THREAD_POOL.put(Integer.toString(key), executor);
            int curCount = ExecutorCounter.incrementAndGet();
            LogUtil.info("SchedulerManager|新建独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key);
        }
        return executor.scheduleAtFixedRate(new RunnableTask(command), initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRateIndependent(String key, Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(key);
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(1);
            SCHEDULE_THREAD_POOL.put(key, executor);
            int curCount = ExecutorCounter.incrementAndGet();
            LogUtil.info("SchedulerManager|新建独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key);
        }
        return executor.scheduleAtFixedRate(new RunnableTask(command), initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRateIndependent(int key, int poolSize, Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(Integer.toString(key));
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(poolSize);
            SCHEDULE_THREAD_POOL.put(Integer.toString(key), executor);
            int curCount = ExecutorCounter.incrementAndGet();
            LogUtil.info("SchedulerManager|新建独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key + "|poolSize:" + poolSize);
        }
        return executor.scheduleAtFixedRate(new RunnableTask(command), initialDelay, period, unit);
    }

    public static void shutDownNow(int key) {
        try {
            ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(Integer.toString(key));
            if (executor == null) return;
            executor.shutdownNow();

            int curCount = ExecutorCounter.decrementAndGet();
            LogUtil.info("SchedulerManager|关闭独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static void shutDownNow(String key) {
        try {
            ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(key);
            if (executor == null) return;
            executor.shutdownNow();

            int curCount = ExecutorCounter.decrementAndGet();
            LogUtil.info("SchedulerManager|关闭独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static void shutDownNow(int key, int poolSize) {
        try {
            ScheduledExecutorService executor = SCHEDULE_THREAD_POOL.get(key);
            if (executor == null) return;
            executor.shutdownNow();

            int curCount = ExecutorCounter.decrementAndGet();
            LogUtil.info("SchedulerManager|关闭独立定时任务线程池|curExecutorCounte:" + curCount + "|ExcutorKey：" + key + "|poolSize:" + poolSize);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

}