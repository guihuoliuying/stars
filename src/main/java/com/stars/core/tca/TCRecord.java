package com.stars.core.tca;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zws on 2015/12/17.
 */
public class TCRecord {

    private String name;
    private long timestamp;
    private long elapse;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    public TCRecord(String name, long timestamp, long elapse) {
        this.name = name;
        this.timestamp = timestamp;
        this.elapse = elapse;
    }

    public String toString() {
        return "{n=" + name + ",t=" + sdf.format(new Date(timestamp)) + ",e=" + elapse + "ms}";
    }

    public static void main(String[] args) {
        List<TCRecord> list = new ArrayList<>();
        list.add(new TCRecord("login.req", System.currentTimeMillis(), 0L));
        list.add(new TCRecord("login.chckAccount", System.currentTimeMillis(), 100L));
        list.add(new TCRecord("login.distLock", System.currentTimeMillis(), 200L));
        list.add(new TCRecord("login.loadAccount", System.currentTimeMillis(), 300L));
        list.add(new TCRecord("login.bindSession", System.currentTimeMillis(), 400L));
        list.add(new TCRecord("login.loadUserData", System.currentTimeMillis(), 500L));
        list.add(new TCRecord("login.init", System.currentTimeMillis(), 1000L));
    }
}
