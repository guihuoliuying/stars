package com.stars.core.actor.invocation;


import com.stars.util.LogUtil;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.annotation.AsyncDelegate;
import com.stars.util.log.CoreLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public class ServiceActor extends AbstractActor {

    private Class clazz;

    private com.stars.core.actor.invocation.InvocationFuture currentFuture;

    // 统计
    private long messageCount;
    private long rpcCount;

    public ServiceActor(int queueSize) {
        super(queueSize);
        for (Class clazz : getClass().getInterfaces()) {
            if (ActorService.class.isAssignableFrom(clazz)) {
                this.clazz = clazz;
                break;
            }
        }
        if (clazz == null) {
            throw new NullPointerException("没有实现Service接口");
        }
    }

    public ServiceActor() {
        this(8192);
    }

    @Override
    public final void onReceived(Object message, Actor sender) {
        try {
            messageCount++;
            if (message instanceof InvocationMessage) {
                rpcCount++;
                InvocationMessage msg = (InvocationMessage) message;
                if (clazz != null) {
                    Method method = clazz.getMethod(msg.methodName, msg.argsTypeArray);
                    if (method != null) {
                        setCurrentFuture(msg.future);
                        try {
                            Object value = method.invoke(this, msg.argsValueArray);
                            if (method.getAnnotation(AsyncDelegate.class) == null) {
                                msg.future.set(value);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            msg.future.setThrowable(e);
                            CoreLogger.error("exception in service's invocation", e);
                        } catch (InvocationTargetException e) {
                            msg.future.setThrowable(e.getTargetException());
                            CoreLogger.error("exception in service's invocation", e);
                        } finally {
                            setCurrentFuture(null);
                        }
                    }
                }
            } else {
                onReceived0(message, sender);
            }
        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    protected void log() {
        LogUtil.info("stat|service:{}|mailboxSize:{}|messageCount:{}|rpcCount:{}",
                getName(), mailboxSize(), messageCount, rpcCount);
    }

    public void onReceived0(Object message, Actor sender) {

    }

    protected com.stars.core.actor.invocation.InvocationFuture getCurrentFuture() {
        return currentFuture;
    }

    private void setCurrentFuture(InvocationFuture currentFuture) {
        this.currentFuture = currentFuture;
    }

}
