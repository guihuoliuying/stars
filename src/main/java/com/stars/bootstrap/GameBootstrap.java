package com.stars.bootstrap;

import com.stars.server.connector.Connector;
import com.stars.server.fight.MultiServer;
import com.stars.server.login2.LoginServer2;
import com.stars.server.main.MainServer;
import com.stars.server.proxy.weaknetwork.NetProxy;
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
            case BootstrapConfig.CONNECTOR:
                server = new Connector(config);
                break;
            case BootstrapConfig.NETPROXY:
                server = new NetProxy();
                break;
            case BootstrapConfig.LOGIN:
                server = new LoginServer2();
                break;
            case BootstrapConfig.FIGHT:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.LOOTTREASURE:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.MULTI:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.RMCHAT:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.FIGHTMANAGER:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.FIGHTMANAGER1:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.PAYSERVER:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.PAYSERVER1:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.FAMILYWAR:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.SKYRANK:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.DAILY5V5:
                server = new com.stars.server.fight.MultiServer(config);
                break;
            case BootstrapConfig.CAMP:
                server = new MultiServer(config);
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
