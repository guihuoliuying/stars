package com.stars.util.callback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2015/12/21.
 */
public class CallbackContext {

    private com.stars.util.callback.CallbackResult result; // 结果：成功，失败，超时
    private String message; // 提示字符串
    private Throwable cause; // 异常
    private Map<Object, Object> attributeMap = new HashMap<>(); // 属性：可用于存放回调参数

    public CallbackContext(com.stars.util.callback.CallbackResult result, String message, Throwable cause) {
        this.result = result;
        this.message = message;
        this.cause = cause;
    }

    public com.stars.util.callback.CallbackResult result() {
        return result;
    }

    public Throwable cause() {
        return cause;
    }

    public String message() {
        return message;
    }

    public void attr(Object key, Object value) {
        attributeMap.put(key, value);
    }

    public Object attr(Object key) {
        return attributeMap.get(key);
    }
    
    public boolean isSuccess() {
    	return result == com.stars.util.callback.CallbackResult.SUCCESS;
    }
    
    public boolean isFailure() {
    	return result == com.stars.util.callback.CallbackResult.FAILURE;
    }
    
    public boolean isTimeout() {
    	return result == CallbackResult.TIMEOUT;
    }
    
}
