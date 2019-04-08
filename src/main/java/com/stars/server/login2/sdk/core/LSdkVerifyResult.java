package com.stars.server.login2.sdk.core;

/**
 * SDK 验证结果
 */
public class LSdkVerifyResult {

    private boolean success; // 是否成功
    private boolean timeout; // 超时

    private String userID;

    private String userName;

    private String nickName;

    private String extension;

    private Object cause;

    public LSdkVerifyResult() {

    }

    public LSdkVerifyResult(boolean suc, String userID, String userName, String nickName){
        this.success = suc;
        this.userID = userID;
        this.userName = userName;
        this.nickName = nickName;
        this.extension = "";
    }


    public LSdkVerifyResult(boolean suc, String userID, String userName, String nickName, String ext){
        this.success = suc;
        this.userID = userID;
        this.userName = userName;
        this.nickName = nickName;
        this.extension = ext;
    }

    public LSdkVerifyResult(boolean suc, Object cause) {
        this.success = suc;
        this.cause = cause;
    }

    public LSdkVerifyResult(boolean suc){
        this.success = suc;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Object getCause() {
        return cause;
    }

    public void setCause(Object cause) {
        this.cause = cause;
    }
}
