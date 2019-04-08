package com.stars.core.clientpatch;

import com.stars.core.schedule.SchedulerManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2017/4/25.
 */
public class PatchManager {

    public static volatile long lastModifiedTime = 0L;
    public static volatile boolean needPatch = false;
    public static volatile String patch = "";

    public static void init() {
        SchedulerManager.scheduleAtFixedRate(new PatchCheckTask(), 30, 30, TimeUnit.SECONDS);
    }

}
