package com.stars.server.login.bean;

/**
 * 账号信息
 *
 * Created by liuyuheng on 2016/1/5.
 */
public class AccountInfo {
    private String account;
    private String pwdMd5;
    private long roleId;
    private long lastLogin;// 上次登录时间

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwdMd5() {
        return pwdMd5;
    }

    public void setPwdMd5(String pwdMd5) {
        this.pwdMd5 = pwdMd5;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}
