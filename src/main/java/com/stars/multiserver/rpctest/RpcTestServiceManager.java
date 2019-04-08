package com.stars.multiserver.rpctest;

import com.stars.multiserver.rpctest.echo.EchoService;
import com.stars.multiserver.rpctest.echo.EchoServicePlainImpl;
import com.stars.services.ServiceManager;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public class RpcTestServiceManager extends ServiceManager {

    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit("echoService", new EchoServicePlainImpl());
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(EchoService.class, getService("echoService")); // 暴露echo服务
//        connectFightServers(); // 连接战斗服
        initRpcHelper(RpcTestRpcHelper.class); // 初始化helper
    }

    @Override
    public void runScheduledJob() throws Throwable {

    }
}
