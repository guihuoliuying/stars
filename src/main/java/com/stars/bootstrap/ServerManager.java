package com.stars.bootstrap;


/**
 * Created by jx on 2015/2/28.
 */
public class ServerManager {

    // 服务
    private static com.stars.bootstrap.AbstractServer server;

    public static String getServerName() {
        return server == null ? "noServer" : server.getName();
    }

    //注册服务
    public static void setServer(com.stars.bootstrap.AbstractServer server) {
        ServerManager.server = server;
    }
    
    //获得服务
    public static AbstractServer getServer() {
        return server;
    }

}
