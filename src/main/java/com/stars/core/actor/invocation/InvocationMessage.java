package com.stars.core.actor.invocation;

import java.util.Arrays;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public class InvocationMessage {

    public long messageId;
    public boolean isAsync = false;
    public String interfaceName;
    public String methodName;
    public Class[] argsTypeArray;
    public Object[] argsValueArray;

    public com.stars.core.actor.invocation.InvocationFuture future;

    public InvocationMessage(String interfaceName, String methodName, Class[] argsTypeArray, Object[] argsValueArray, com.stars.core.actor.invocation.InvocationFuture future) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.argsTypeArray = argsTypeArray;
        this.argsValueArray = argsValueArray;
        this.future = future;
    }

    public InvocationMessage(String methodName, Class[] argsTypeArray, Object[] argsValueArray, InvocationFuture future) {
        this.methodName = methodName;
        this.argsTypeArray = argsTypeArray;
        this.argsValueArray = argsValueArray;
        this.future = future;
    }

    @Override
    public String toString() {
        return "InvocationMessage{" +
                "messageId=" + messageId +
                ", isAsync=" + isAsync +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", argsTypeArray=" + Arrays.toString(argsTypeArray) +
                ", argsValueArray=" + Arrays.toString(argsValueArray) +
                ", future=" + future +
                '}';
    }
}
