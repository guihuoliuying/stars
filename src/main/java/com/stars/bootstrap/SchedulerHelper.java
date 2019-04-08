package com.stars.bootstrap;

import com.stars.util.log.CoreLogger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Objects;

/**
 * 为定时器提供一个全局统一访问点
 * Created by zws on 2015/6/1.
 */
public class SchedulerHelper {

    private static Scheduler scheduler;

    public static synchronized void setScheduler(Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        SchedulerHelper.scheduler = scheduler;
    }

    public static synchronized Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * 初始化
     * @param path 配置文件的路径
     * @throws SchedulerException
     */
    public static synchronized void initAndStart(String path) throws SchedulerException {
        Objects.requireNonNull(path);
        SchedulerFactory schedulerFactory;
        try {
            com.stars.util.log.CoreLogger.info("初始化定时任务[{}]", path);
            schedulerFactory = new StdSchedulerFactory(path);
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            com.stars.util.log.CoreLogger.error("初始化定时任务异常");
            throw e;
        }
    }

    public static synchronized void initAndStart() throws SchedulerException {
        SchedulerFactory schedulerFactory;
        try {
            com.stars.util.log.CoreLogger.info("初始化定时任务[没配置文件]");
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            com.stars.util.log.CoreLogger.error("初始化定时任务异常");
            throw e;
        }
    }

    /**
     * 初始化
     * @param path 配置文件的路径
     * @throws SchedulerException
     */
    public static synchronized void init(String path) throws SchedulerException {
        Objects.requireNonNull(path);
        SchedulerFactory schedulerFactory;
        try {
            com.stars.util.log.CoreLogger.info("初始化定时任务[{}]", path);
            schedulerFactory = new StdSchedulerFactory(path);
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            com.stars.util.log.CoreLogger.error("初始化定时任务异常");
            throw e;
        }
    }

    /**
     * 初始化
     * @param path 配置文件的路径
     * @throws SchedulerException
     */
    public static synchronized void init() throws SchedulerException {
        SchedulerFactory schedulerFactory;
        try {
            com.stars.util.log.CoreLogger.info("初始化定时任务[没配置文件]");
            schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            com.stars.util.log.CoreLogger.error("初始化定时任务异常");
            throw e;
        }
    }

    /**
     * 初始化
     * @param path 配置文件的路径
     * @throws SchedulerException
     */
    public static synchronized void start() throws SchedulerException {
        Objects.requireNonNull(scheduler);
        try {
            com.stars.util.log.CoreLogger.info("启动定时任务");
            scheduler.start();
        } catch (SchedulerException e) {
            CoreLogger.error("启动定时任务异常");
            throw e;
        }
    }

}
