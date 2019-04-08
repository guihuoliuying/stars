package com.stars.server.login.bean;

/**
 * 客户端登录信息
 * Created by liuyuheng on 2016/1/5.
 */
public class LoginInfo {
    private String account;
    private String password;

    private String channelId;// 渠道码,验证需要
    private String channelId_sub;// 子渠道码
    private boolean isChannelChecked;// 渠道已验证
    private String authcode;//验证码
    private String mac;

    public LoginInfo(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId_sub() {
        return channelId_sub;
    }

    public void setChannelId_sub(String channelId_sub) {
        this.channelId_sub = channelId_sub;
    }

    public boolean isChannelChecked() {
        return isChannelChecked;
    }

    public void setChannelChecked(boolean isChannelChecked) {
        this.isChannelChecked = isChannelChecked;
    }

    public String getAuthcode() {
        return authcode;
    }

    public void setAuthcode(String authcode) {
        this.authcode = authcode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
