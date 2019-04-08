package com.stars.modules.demologin.message;

import com.stars.network.server.session.GameSession;

/**
 * Created by liuyuheng on 2016/6/17.
 */
public class CreateRoleMsg extends LoginSyncMsg {
    private String account;
    private long roleId;
    private com.stars.network.server.session.GameSession session;

    public CreateRoleMsg(String account, long roleId, com.stars.network.server.session.GameSession session) {
        this.account = account;
        this.roleId = roleId;
        this.session = session;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @Override
    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Override
    public GameSession getSession() {
        return session;
    }
}
