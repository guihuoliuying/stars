package com.stars.modules.demologin.message;

import com.stars.network.server.session.GameSession;

/**
 * Created by liuyuheng on 2016/6/17.
 */
public class LoginMsg extends LoginSyncMsg {
    private String account;
    private long roleId;
    private int roleIdVersion; // 用于设置roleId
    private com.stars.network.server.session.GameSession session;
    private boolean isCreation;

//    public LoginMsg(String account, long roleId, GameSession session) {
//        this.account = account;
//        this.roleId = roleId;
//        this.session = session;
//    }

    public LoginMsg(String account, long roleId, int roleIdVersion, com.stars.network.server.session.GameSession session, boolean isCreation) {
        this.account = account;
        this.roleId = roleId;
        this.roleIdVersion = roleIdVersion;
        this.session = session;
        this.isCreation = isCreation;
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

    @Override
    public GameSession getSession() {
        return session;
    }

    public boolean isCreation() {
        return isCreation;
    }
}
