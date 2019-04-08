package com.stars.modules.demologin.message;

/**
 * Created by chenkeyu on 2017-07-19.
 */
public class PullUpMsg extends LoginSyncMsg {
    private String account;
    private long roleId;
    private int roleIdVersion; // 用于设置roleId
    private boolean isCreation;
    private int snapChannelId;

    public PullUpMsg(String account, long roleId, int roleIdVersion, boolean isCreation, int snapChannel) {
        this.account = account;
        this.roleId = roleId;
        this.roleIdVersion = roleIdVersion;
        this.isCreation = isCreation;
        this.snapChannelId = snapChannel;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    public int getRoleIdVersion() {
        return roleIdVersion;
    }

    public boolean isCreation() {
        return isCreation;
    }

    public int getSnapChannelId() {
        return snapChannelId;
    }
}
