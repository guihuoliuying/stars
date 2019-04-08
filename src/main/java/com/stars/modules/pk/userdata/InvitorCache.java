package com.stars.modules.pk.userdata;

/**
 * Created by liuyuheng on 2016/11/1.
 */
public class InvitorCache {
    private long invitorId;// 邀请者roleId
    private String invitorName;// 邀请者名称
    private long createTimestamp;

    public InvitorCache(long invitorId, String invitorName) {
        this.invitorId = invitorId;
        this.invitorName = invitorName;
        createTimestamp = System.currentTimeMillis();
    }

    public long getInvitorId() {
        return invitorId;
    }

    public String getInvitorName() {
        return invitorName;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }
}
