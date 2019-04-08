package com.stars;

import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.lang.management.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.management.ManagementFactory.*;

/**
 * Created by zhouyaohui on 2016/12/27.
 */
public class ServerStatePrinter {

    public final static long MB = 1024 * 1024;
    private static ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    @SuppressWarnings("")
    public static void init() {
        schedule.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerLogConst.state.info("=== print state ===");
                    logMemory();
                    logHeapMemory();
                    logNonHeapMemory();
                    logBufferPool();
                    logGc();
                    logThread();
                    ServerLogConst.state.info("===========");
                } catch (Exception e) {
                    com.stars.util.LogUtil.error("", e);
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private static void logMemory() {
        long freeMemory = Runtime.getRuntime().freeMemory() / MB;   // 空闲内存
        long totalMemory = Runtime.getRuntime().totalMemory() / MB; // 当前内存
        long maxMemory = Runtime.getRuntime().maxMemory() / MB; // 最大可使用内存
        com.stars.util.LogUtil.info("stat|memory|free:{}M|used:{}M|total:{}M|max:{}M|objectPendingFinalizationCount:{}",
                freeMemory, totalMemory - freeMemory, totalMemory, maxMemory, getMemoryMXBean().getObjectPendingFinalizationCount());
    }

    private static void logHeapMemory() {
        MemoryUsage usage = getMemoryMXBean().getHeapMemoryUsage();
        com.stars.util.LogUtil.info("stat|heap|init:{}M|used:{}M|committed:{}M|max:{}M",
                usage.getInit()/MB, usage.getUsed()/MB, usage.getCommitted()/MB, usage.getMax()/MB);
    }

    private static void logNonHeapMemory() {
        MemoryUsage usage = getMemoryMXBean().getNonHeapMemoryUsage();
        com.stars.util.LogUtil.info("stat|nonheap|init:{}M|used:{}M|committed:{}M|max:{}M",
                usage.getInit()/MB, usage.getUsed()/MB, usage.getCommitted()/MB, usage.getMax()/MB);
    }

    private static void logBufferPool() {
        StringBuilder sb = new StringBuilder();
        for (BufferPoolMXBean mxbean : (List<BufferPoolMXBean>) getPlatformMXBeans(BufferPoolMXBean.class)) {
            com.stars.util.LogUtil.info("stat|bufferpool|{}|count:{}|used:{}M|total:{}M",
                    mxbean.getName(), mxbean.getCount(), mxbean.getMemoryUsed()/MB, mxbean.getTotalCapacity()/MB);
        }
    }

    private static void logGc() {
        StringBuilder sb = new StringBuilder();
        List<GarbageCollectorMXBean> list = getGarbageCollectorMXBeans();
        for (int i = 0; i < list.size(); i++) {
            GarbageCollectorMXBean mxbean = list.get(i);
            if (mxbean != null) {
                sb.append(mxbean.getName()).append(":")
                        .append(mxbean.getCollectionCount()).append(":")
                        .append(mxbean.getCollectionTime()).append("ms");
                if (i != list.size() - 1) {
                    sb.append("|");
                }
            }
        }
        com.stars.util.LogUtil.info("stat|gc|{}", sb.toString());
    }

    private static void logThread() {
        ThreadMXBean mxbean = getThreadMXBean();
        int threadCount = mxbean.getThreadCount();
        int daemonThreadCount = mxbean.getDaemonThreadCount();
        int peakThreadCount = mxbean.getPeakThreadCount();

        int newCount = 0;
        int runnableCount = 0;
        int blockedCount = 0;
        int watingCount = 0;
        int timedWaitingCount = 0;
        int terminatedCount = 0;
        for (long id : mxbean.getAllThreadIds()) {
            ThreadInfo info = mxbean.getThreadInfo(id);
            if (info == null) {
                continue;
            }
            Thread.State state = info.getThreadState();
            switch (state) {
                case NEW:
                    newCount++;
                    break;
                case RUNNABLE:
                    runnableCount++;
                    break;
                case BLOCKED:
                    blockedCount++;
                    break;
                case WAITING:
                    watingCount++;
                    break;
                case TIMED_WAITING:
                    timedWaitingCount++;
                    break;
                case TERMINATED:
                    terminatedCount++;
                    break;
            }
        }

        LogUtil.info("stat|thread|current:{}|daemon:{}|peak:{}|new:{}|runnable:{}|blocked:{}|waiting:{}|timedWaiting:{}|terminated:{}",
                threadCount, daemonThreadCount, peakThreadCount, newCount, runnableCount, blockedCount, watingCount, timedWaitingCount, terminatedCount);
    }

}
