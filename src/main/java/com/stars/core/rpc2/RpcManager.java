package com.stars.core.rpc2;

import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.InvocationMessage;
import com.stars.core.rpc2.packet.RpcInvocationReq;
import com.stars.core.rpc2.packet.RpcRegistrationReq;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/11/2.
 */
public class RpcManager {

    public static ConcurrentMap<Integer, GameSession> sessionMap = new ConcurrentHashMap<>(); // serverId -> session，
    public static ConcurrentMap<Integer, RpcClientConnectedCallback> callbackMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Object> serviceMap = new ConcurrentHashMap<>(); // 服务实现类的实例
    public static ConcurrentMap<String, Class> classMap =  new ConcurrentHashMap<>(); // 服务接口Map
    public static ConcurrentHashMap<Integer, RpcClient>rcpClientMap = new ConcurrentHashMap<Integer, RpcClient>();

    public static void regRpcService(Class clazz, Object service) {
        classMap.put(clazz.getName(), clazz);
        serviceMap.put(clazz.getName(), service);
    }

    public static boolean handlePacket(Packet packet) {
        if (packet instanceof RpcRegistrationReq) { // Rpc客户端连接注册包
            RpcRegistrationReq req = (RpcRegistrationReq) packet;
            for (int serverId : req.getServerIdList()) {
                sessionMap.put(serverId, req.getSession());
            }
            com.stars.util.LogUtil.info("RPC服务接受连接{}", req.getServerIdList());
            return true;

        } else if (packet instanceof RpcInvocationReq) { // Rpc调用请求
            InvocationMessage im = null;
            try {
                im = ((RpcInvocationReq) packet).getInvocationMessage();
                Object service = RpcManager.serviceMap.get(im.interfaceName); // 获取服务实现类的实例
                Class clazz = RpcManager.classMap.get(im.interfaceName); // 获取服务接口
                Method method = clazz.getMethod(im.methodName, im.argsTypeArray); // 获取调用的方法
                method.invoke(service, im.argsValueArray); // 调用
            } catch (Throwable t) {
                if (im == null) {
                    com.stars.util.LogUtil.error("Rpc|调用异常|RpcInvocationReq:null", t);
                } else {
                    LogUtil.error("Rpc|调用异常|interfaceName:" + im.interfaceName + "|methodName:" + im.methodName + "|argsValueArray:" + Arrays.toString(im.argsValueArray), t);
                }
            }
            return true;
        }
        return false;
    }

}

// 暂不使用
class RpcInvocationTask implements Runnable {
    private RpcInvocationReq req;
    public RpcInvocationTask(RpcInvocationReq req) { this.req = req; }
    @Override
    public void run() {
        try {
            com.stars.core.actor.invocation.InvocationMessage im = req.getInvocationMessage();
            Object service = RpcManager.serviceMap.get(im.interfaceName);
            Class clazz = RpcManager.classMap.get(im.interfaceName);
            Method method = clazz.getMethod(im.methodName, im.argsTypeArray);
            method.invoke(service, im.argsValueArray);
        } catch (Throwable t) {
            com.stars.util.LogUtil.error("", t);
        }
    }
}
