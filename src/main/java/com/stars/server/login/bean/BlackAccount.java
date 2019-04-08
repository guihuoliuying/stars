package com.stars.server.login.bean;

/**
 * 黑名单
 *
 * Created by liuyuheng on 2016/1/5.
 */
public class BlackAccount {
    private String account;
    private long overTime;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getOverTime() {
        return overTime;
    }

    public void setOverTime(long overTime) {
        this.overTime = overTime;
    }
}
