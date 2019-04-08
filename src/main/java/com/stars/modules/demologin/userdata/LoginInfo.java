package com.stars.modules.demologin.userdata;

/**
 * 登陆包信息
 *
 * @author huangdimin
 */
public class LoginInfo {
    public String uid = null;
    public String sid = null;
    public String channel = null;
    public String channelSub = null;
    public String phoneSystem = null;//手机系统安卓/ios...
    public String phoneNet = null;//手机运营商电信/移动/联通...
    public String net = null;//手机网络wifi/4G/...
    public String phoneType = null;//手机型号
    public String verision = null;//"客户端版本"
    public String imei = null;
    public String mac = null;
    public String regChannel = null;//注册渠道
    public String ip = null;
    public String platForm = "4";
    public String accoutRegisterTime = "2016-06-06 06:06:06";
    public String sdkVersion = "2.2.0";
    public String osVersion = "";
    public String idfa = null;
    public String gameId = null;
    public String userId = null;


    private String account = null;
    private String password = null;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelSub() {
        if (channel != null) {
            return channel.split("@")[1];
        }
        return "0";
    }

    public String getPhoneSystem() {
        return phoneSystem;
    }

    public void setPhoneSystem(String phoneSystem) {
        this.phoneSystem = phoneSystem;
        if (phoneSystem != null) {
            if (phoneSystem.equalsIgnoreCase("Android")) {
                this.platForm = "4";
            } else if (phoneSystem.equalsIgnoreCase("IOS")) {
                this.platForm = "5";
            } else {
                this.platForm = "0";
            }
        }
    }

    public String getPhoneNet() {
        return phoneNet;
    }

    public void setPhoneNet(String phoneNet) {
        this.phoneNet = phoneNet;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    public String getVerision() {
        return verision;
    }

    public void setVerision(String verision) {
        this.verision = verision;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public String getRegChannel() {
        if (channel != null) {
            return channel.split("@")[1];
        }
        return "0";
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPlatForm() {
        return platForm;
    }

    public void setPlatForm(String platForm) {
        this.platForm = platForm;
    }

    public String getAccoutRegisterTime() {
        return accoutRegisterTime;
    }

    public void setAccoutRegisterTime(String accoutRegisterTime) {
        this.accoutRegisterTime = accoutRegisterTime;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "uid='" + uid + '\'' +
                ", sid='" + sid + '\'' +
                ", channel='" + channel + '\'' +
                ", channelSub='" + channelSub + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
