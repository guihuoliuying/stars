package com.stars.server.main;

import com.stars.bootstrap.AbstractServer;
import com.stars.bootstrap.BootstrapConfig;
import com.stars.network.server.MainNet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.util.ExecuteManager;
import com.stars.util.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jx on 2015/2/27.
 */
public class MainServer extends AbstractServer {

    private static com.stars.server.Business business;
    public static AtomicInteger payload = new AtomicInteger(0);

    public MainServer(BootstrapConfig config) {
        super(BootstrapConfig.MAIN);
        LogUtil.init(); // 初始化日志工具
//        config.initServerConfig();
//        config.initCommonServerConfig();
        setConfig(config);
        try {
            setStartTimestamp();
            business = (com.stars.server.Business) ClassLoader.getSystemClassLoader().loadClass(config.getBusinessName()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("找不到业务处理类" + config.getBusinessName());
        }
    }

    @Override
    public void start() throws Exception {
        ExecuteManager.init(); // 线程池初始化
        PacketManager.loadCorePacket(); // 加载底层数据包
//        DbUtil.init(); // 数据库连接池初始化
        business.init(); // 业务层初始化
        MainNet.startup(getConfig().getServerPort()); // 启动网络层
        setOn();
    }

    @Override
    public void stop() {
        business.clear();
    }

    @Override
    public String getName() {
        return BootstrapConfig.MAIN;
    }

    public static Business getBusiness() {
        return business;
    }

//    private void signalLauncher() throws Exception {
//        CommonServerManager.send(new BusinessFinishM2lPacket());
//    }

}
