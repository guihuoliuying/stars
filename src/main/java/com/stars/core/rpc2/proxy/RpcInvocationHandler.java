package com.stars.core.rpc2.proxy;

import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.InvocationFuture;
import com.stars.core.actor.invocation.InvocationMessage;
import com.stars.core.rpc2.RpcManager;
import com.stars.core.rpc2.packet.RpcInvocationReq;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * 动态代理的处理方法
 * Created by zhaowenshuo on 2016/11/2.
 */
public class RpcInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据第一个参数（必定为serverId）找到对应的session
        checkFirstArg(args);
        int serverId = (int) args[0];
        GameSession session = RpcManager.sessionMap.get(serverId);
        if (session == null || session.getChannel() == null || !session.getChannel().isActive()) {
            boolean isSessionNull = session == null;
            boolean isChannelNull = true;
            boolean isChannelActive = false;
            if (!isSessionNull) {
                isChannelNull = session.getChannel() == null;
            }
            if (!isSessionNull && !isChannelNull) {
                isChannelActive = session.getChannel().isActive();
            }
        	com.stars.util.LogUtil.error("rpc: can not find session serverId={}, session is null?{}, channel is null?{}, channel is active?{}",
                    serverId, isSessionNull, isChannelNull, isChannelActive);
            throw new IllegalStateException();
        }
        // 封装Rpc调用请求
        com.stars.core.rpc2.packet.RpcInvocationReq req = new RpcInvocationReq();
        Class clazz = getInterface(method);
        com.stars.core.actor.invocation.InvocationMessage im = new InvocationMessage(
                clazz.getName(), method.getName(), method.getParameterTypes(), args, new InvocationFuture());
        req.setInvocationMessage(im);
        // 发送Rpc调用请求
        PacketManager.send(session, req);
        return null;
    }

    private void checkFirstArg(Object[] args) throws IllegalArgumentException {
        if (args == null || args.length < 1 || !(args[0] instanceof Integer)) {
            LogUtil.error("rpc: 1st args is incorrect: {}", Arrays.toString(args));
            throw new IllegalArgumentException();
        }
    }

    private Class getInterface(Method method) {
        return method.getDeclaringClass();
    }

    public static void main(String[] args) {
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { Runnable.class, Comparable.class }, new RpcInvocationHandler());
        System.out.println(Arrays.toString(proxy.getClass().getInterfaces()));
    }

}
