package com.stars.services.marry.event;

import com.stars.core.event.Event;

/**
 * Created by zhoujin on 2017/4/13.
 */
public class MarryAppointSceneCheckEvent extends Event {

    private long appoinder;
    private long appoinded;
    private byte gerder;
    private byte appType;

    public MarryAppointSceneCheckEvent(long appoinder, long appoinded,byte gerder, byte appType) {
        this.appoinder = appoinder;
        this.appoinded = appoinded;
        this.gerder = gerder;
        this.appType = appType;
    }

    public long getAppoinder() {
        return this.appoinder;
    }

    public long getAppoinded() {
        return this.appoinded;
    }

    public byte getGerder() {
        return this.gerder;
    }

    public byte getAppType() {
        return this.appType;
    }
}
