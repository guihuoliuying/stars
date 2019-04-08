package com.stars.server.login;

import com.stars.bootstrap.AbstractServer;
import com.stars.bootstrap.BootstrapConfig;
import com.stars.util.LogUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuyuheng on 2015/12/28.
 */
public class LoginServer extends AbstractServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private String ip;
    private int port;
    private int threadNumber;

    public static volatile boolean isRunning = true;
    
    private BootstrapConfig config;

    public LoginServer(BootstrapConfig config) {
        super(BootstrapConfig.LOGIN);
        this.config = config;
    }

    private void initConfig() throws Exception {
    	Properties p = config.getServerProp();
        setIp(p.getProperty("serverIp"));
        setPort(Integer.parseInt(p.getProperty("serverPort")));
        setThreadNumber(Integer.parseInt(p.getProperty("login-server-threadNumber")));
        LoginConstant.returnIp = p.getProperty("login-server-return-ip");
        LoginConstant.returnPort = Integer.parseInt(p.getProperty("login-server-return-port"));
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(threadNumber);
    }

    @Override
    public void start() throws Exception {
        try {
        	com.stars.util.LogUtil.init();
            // 加载配置
            initConfig();
//            CoreLogger.init("loginServer"); // 初始化日志

//            ConfigClient.start(config).get();
//            LoginServerUtil.initUserClient(new UserDbCallback());
//            LoginServerUtil.initCommClient(new CommDbCallback());
            CountDownLatch latch = new CountDownLatch(2);
//            LoginManager.manager.loadAccount(latch);
//            LoginManager.manager.loadRoleId(latch);
            latch.await(5000L, TimeUnit.MILLISECONDS);
            com.stars.util.LogUtil.info("正在启动服务[{}]...", "loginServer");
            LoginServerNet.bind(bossGroup, workerGroup, ip, port);
//            LoginServerUtil.initIdGenerator(); // 初始化ID生成器
            com.stars.util.LogUtil.info("服务器[{}]启动成功", "loginServer");
        } catch (Exception e) {
            isRunning = false;
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
            LogUtil.error("服务器启动失败", e);
        }
    }

    @Override
    public void stop() {
        System.out.println("停止登录服...");
        isRunning = false;
        /* 停止线程 */
        System.out.println("停止IO线程...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
        /* 完成 */
        System.out.println("登录服已停止");
    }

//    static class UserDbCallback implements DbProxyFaultCallback {
//
//        @Override
//        public void onException() {
//            CoreLogger.info("用户数据服异常, 请检查是否宕机");
//        }
//    }
//
//    static class CommDbCallback implements DbProxyFaultCallback {
//
//        @Override
//        public void onException() {
//            CoreLogger.info("公共数据服异常, 请检查是否宕机");
//        }
//    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }
}
