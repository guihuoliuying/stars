package com.stars.bootstrap;

import com.stars.server.main.MainServer;
import com.stars.util.log.CoreLogger;

import java.io.IOException;

/**
 * Created by jx on 2015/3/2.
 */
public class GameBootstrap {

    private BootstrapConfig config;//服务启动配置类

    private static String serverType = "";

    public GameBootstrap() {
    }

    //加载服务启动配置
    public void loadConfig(String server) throws IOException {
        if (server == null || server.equals("")) {
            server = "server";
        }
        this.config = new BootstrapConfig(server);
    }

    //启动服务
    public void start() throws Exception {
        CoreLogger.info("正在启动服务[{}]...", config.getServerName());
//        String serverName = config.getServer();
        String serverType = config.getServerType();
        AbstractServer server;
        GameBootstrap.setServerType(serverType);
        switch (serverType) {
            case BootstrapConfig.MAIN:
                server = new MainServer(config);
                break;
            default:
                throw new RuntimeException("配置错误");
        }
        ServerManager.setServer(server);
        server.start();
        server.setOn();
        addShutdownHook(server);
    }

    //添加相关服务关闭钩子
    public static void addShutdownHook(Server server) {
        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(server));
    }

    public static String getServerType() {
        return serverType;
    }

    public static void setServerType(String serverType) {
        GameBootstrap.serverType = serverType;
    }
}
