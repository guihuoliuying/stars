package com.stars.util;

import com.stars.util.log.CoreLogger;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xieyuejun
 *         <p/>
 *         线程池管理
 */
public class ExecuteManager {

    public static int corePoolSize = 48;
    public static int maximumPoolSize = 48;

    public static long keepAliveTime = 3;
    public static int taskQueueSize = 10240;

    public static int scheduledCorePoolSize = 10;

    public static ThreadPoolExecutor executor;

    public static void init() {
        executor = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque(taskQueueSize));
        executor.prestartAllCoreThreads(); // 等待所有核心线程启动
    }

    public static void init(int maxSize) {
        executor = new ThreadPoolExecutor(
                maxSize, maxSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque(taskQueueSize));
        executor.prestartAllCoreThreads(); // 等待所有核心线程启动
    }

    public static void execute(final Runnable task) {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Throwable cause) {
                        CoreLogger.error("线程池执行报错", cause);
                        if (cause instanceof Error) { // 如果是错误则重新抛出
                            throw cause;
                        }
                    }
                }
            });
        } catch (Exception e) {
            CoreLogger.error("线程池执行报错", e);
        }
    }

    public static void execWithThrowable(Runnable task) {
        executor.execute(task);
    }

}
