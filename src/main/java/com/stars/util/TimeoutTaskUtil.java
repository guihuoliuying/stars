package com.stars.util;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 超时任务工具类
 * <p/>
 * Created by zd on 2015/5/25.
 */
public class TimeoutTaskUtil {

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private static AtomicInteger id = new AtomicInteger();
    private static ConcurrentMap<Integer, TimeoutTask> timeoutTasks = new ConcurrentHashMap<>();

    static {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                for (Map.Entry<Integer, TimeoutTask> e : timeoutTasks.entrySet()) {
                    TimeoutTask task = e.getValue();
                    if (task.getTime() > current) {
                        task = timeoutTasks.remove(e.getKey());
                        if (task != null) {
                            ExecuteManager.execute(task);
                        }
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 添加一个超时任务
     *
     * @param seconds     超时时间，单位是秒
     * @param timeoutTask 超时任务
     * @return 超时任务id
     */
    public static int addTimeoutTask(int seconds, TimeoutTask timeoutTask) {
        long t = System.currentTimeMillis() + seconds * 1000;
        timeoutTask.setTime(t);
        int requestId = id.incrementAndGet();
        timeoutTasks.put(requestId, timeoutTask);
        return requestId;
    }

    /**
     * 删除超时任务
     *
     * @param requestId 超时任务id
     */
    public static void removeTimeoutTask(int requestId) {
        timeoutTasks.remove(requestId);
    }

    public static abstract class TimeoutTask implements Runnable {

        private long time;

        private long getTime() {
            return time;
        }

        private void setTime(long time) {
            this.time = time;
        }

        public abstract void execute();

        @Override
        public final void run() {
            execute();
        }

    }

}
