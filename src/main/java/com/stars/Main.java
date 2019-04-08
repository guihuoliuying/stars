package com.stars;


import com.stars.bootstrap.GameBootstrap;
import com.stars.bootstrap.ServerManager;
import com.stars.util.log.CoreLogger;

/**
 * Created by jx on 2015/2/28.
 */
public class Main {
    public static void main(String [] args) throws Exception {
    	long st = System.currentTimeMillis();
    	String parString = "";
    	if (args != null && args.length > 0) {
			parString = args[0];
		}
        com.stars.util.log.CoreLogger.init(parString); // 初始化日志
        GameBootstrap bootstrap = new GameBootstrap();
        try {
            bootstrap.loadConfig(parString); // 加载配置
            bootstrap.start(); // 启动服务
            long et = System.currentTimeMillis();
            com.stars.util.log.CoreLogger.info("服务器[{}]启动成功, 耗时: {}ms", ServerManager.getServerName(), (et - st));
        } catch (Exception e) {
            CoreLogger.error("服务器启动失败", e);
            System.exit(1);
        }
    }
}
