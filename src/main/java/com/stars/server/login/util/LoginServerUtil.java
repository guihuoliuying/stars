package com.stars.server.login.util;

import com.stars.util.uid.IdGenerator;

/**
 * Created by liuyuheng on 2016/1/6.
 */
public class LoginServerUtil {
    public static IdGenerator idGenerator;

//    private static DbProxyClient userClient; // 用户dbServer客户端
//    private static DbProxyClient commClient; // 公共dbServer客户端
//
//    public static void initIdGenerator() throws Exception {
//        CoreLogger.info("初始化ID生成器...");
//        idGenerator = new IdGenerator();
//        // TODO: 游戏大区是写死的
//        idGenerator.register("roleId", new RoleIdAllocator(
//                userClient, 2, ServerConfig.serverId));
//        idGenerator.init();
//    }
//
//    public static void initUserClient(DbProxyFaultCallback callback)
//            throws Exception {
//        RoleAllocRouteStrategy strategy = new RoleAllocRouteStrategy();
//        userClient = createClient("db-mapping", strategy, callback);
//    }
//
//    public static void initCommClient(DbProxyFaultCallback callback)
//            throws Exception {
//        CommRouteStrategy strategy = new CommRouteStrategy();
//        commClient = createClient("comm-mapping", strategy, callback);
//    }
//
//    private static DbProxyClient createClient(String configType,
//                                              DbProxyRouteStrategy routeStrategy,
//                                              DbProxyFaultCallback callback) throws Exception {
//        DbProxyClient client = new DbProxyClient(); // 用户库
//        client.loadConfig(BootstrapConfig.MAPPING_PATH, configType);
//        client.initRouteStrategy(routeStrategy);
//        client.registerFaultHandler(callback);
//        client.initConnections();
//        return client;
//    }
//
//    public static IdGenerator getIdGenerator() {
//        return idGenerator;
//    }
//
//    public static void setIdGenerator(IdGenerator idGenerator) {
//        LoginServerUtil.idGenerator = idGenerator;
//    }
//
//    public static DbProxyClient getUserClient() {
//        return userClient;
//    }
//
//    public static void setUserClient(DbProxyClient userClient) {
//        LoginServerUtil.userClient = userClient;
//    }
//
//    public static DbProxyClient getCommClient() {
//        return commClient;
//    }
//
//    public static void setCommClient(DbProxyClient commClient) {
//        LoginServerUtil.commClient = commClient;
//    }
}
