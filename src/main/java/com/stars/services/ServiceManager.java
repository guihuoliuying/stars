package com.stars.services;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.actor.invocation.ServiceActorHelper;
import com.stars.core.rpc2.RpcClient;
import com.stars.core.rpc2.RpcClientConnectedCallback;
import com.stars.core.rpc2.RpcManager;
import com.stars.core.rpc2.proxy.RpcInvocationHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public abstract class ServiceManager {

    private Map<String, Service> serviceMap = new HashMap<>();

    private Map<String, Class> exportedInterfaceMap = new HashMap<>();
    private Map<String, Object> exportedServiceMap = new HashMap<>();

    /**
     * 初始化自身服务
     * @throws Throwable
     */
    public abstract void initSelfServices() throws Throwable;

    /**
     * 初始化RPC流程
     * 1. 导出服务
     * 2. 初始化连接
     *   2.1 主服调用connectMultiServers() -> 战斗服/跨服
     *   2.2 跨服调用connectFightServers() -> 战斗服）
     * 3. 初始化RpcHelper（类似于ServiceHelper，由业务编写者定义）
     * @throws Throwable
     */
    public abstract void initRpc() throws Throwable;

    public abstract void runScheduledJob() throws Throwable;

    public void init() throws Throwable {
        initSelfServices(); // 初始化自身服务
        initRpc(); // 初始化Rpc
    }

    /**
     * 为actor生成一个动态代理
     * @param actor
     * @return
     */
    protected Service newService(com.stars.core.actor.invocation.ServiceActor actor) {
        return (Service) com.stars.core.actor.invocation.ServiceActorHelper.newService(actor);
    }

    /**
     * 为actor生成一个动态代理
     * @param clazz
     * @param num
     * @return
     * @throws Throwable
     */
    protected Service newDispatchService(Class<? extends com.stars.core.actor.invocation.ServiceActor> clazz, int num) throws Throwable {
        Constructor<? extends com.stars.core.actor.invocation.ServiceActor> constructor = clazz.getDeclaredConstructor(int.class);
        com.stars.core.actor.invocation.ServiceActor[] actors = new ServiceActor[num];
        for (int i = 0; i < num; i++) {
            actors[i] = constructor.newInstance(i);
        }
        return (Service) ServiceActorHelper.newDispatchService(actors);
    }

    /**
     * 注册并初始化服务
     * @param name
     * @param service
     * @throws Throwable
     */
    public void registerAndInit(String name, Service service) throws Throwable {
        long s = System.currentTimeMillis();
        service.init();
        long e = System.currentTimeMillis();
        serviceMap.put(name, service);
        ServiceHelper.initField(name);
        com.stars.util.LogUtil.info("注册并初始化服务[" + name + "], 耗时=" + (e - s) / 1000.0 + "s");
    }

    /**
     * 注册服务
     * @param name
     * @param service
     * @throws Throwable
     */
    public void register(String name, Service service) throws Throwable {
        serviceMap.put(name, service);
        ServiceHelper.initField(name);
        LogUtil.info("注册服务[" + name + "]");
    }

    /**
     * 根据名称获取服务实现类的实例
     * @param name
     * @return
     */
    public Service getService(String name) {
        return serviceMap.get(name);
    }

    /**
     * 暴露服务，给对方提供调用
     * @param clazz
     * @param service
     * @throws Exception
     */
    protected void exportService(Class clazz, Service service) throws Exception {
         RpcManager.regRpcService(clazz, service);
    }

    protected void connectServer(String serverName) throws Exception {
        new com.stars.core.rpc2.RpcClient(serverName, null).connect();
    }

    protected void connectServer(String serverName, com.stars.core.rpc2.RpcClientConnectedCallback callback) throws Exception {
        new com.stars.core.rpc2.RpcClient(serverName, callback).connect();
    }
    
    protected void connectServer(String serverName, RpcClientConnectedCallback callback, Runnable netExceptionTask) throws Exception {
        new RpcClient(serverName, callback,netExceptionTask).connect();
    }
    
    /**
     * 主服 --> 战斗服
     * 主服 --> 跨服
     * 连接跨服和战斗服，通常在主服调用
     * @throws Exception
     */
    @Deprecated
    protected void connectMultiServers() throws Exception {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        ConcurrentMap<String, Properties> propsMap = config.getProps();
        for (Map.Entry<String, Properties> entry : propsMap.entrySet()) {
            if (entry.getKey().startsWith("fight")
                    || entry.getKey().startsWith("loot")
                    || entry.getKey().startsWith("multi")) {
                connectServer(entry.getKey());
            }
        }
    }


    /**
     * 初始化Helper类，为每个属性生成一个代理类
     * @param helper
     * @throws Exception
     */
    protected void initRpcHelper(Class helper) throws Exception {
        Field[] fields = helper.getDeclaredFields();
        for (Field f : fields) {
            if (Service.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                f.set(helper, Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[] { f.getType() },
                        new RpcInvocationHandler()));
            }
        }
    }

}
