package com.stars.modules.tool.func;

import com.stars.modules.demologin.packet.ClientText;

/**
 * 道具使用前检查的返回结果
 * Created by zhaowenshuo on 2016/5/16.
 */
public class ToolFuncResult {

    private boolean isSuccess; // 是否成功
    private ClientText message; // 提示

    public ToolFuncResult(boolean isSuccess, ClientText message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public ClientText getMessage() {
        return message;
    }

    public void setMessage(ClientText message) {
        this.message = message;
    }
}
