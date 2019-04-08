package com.stars.bootstrap;

import com.stars.util.log.CoreLogger;

/**
 * Created by jx on 2015/3/2.
 */
public class ShutdownHookThread extends Thread {

    private com.stars.bootstrap.Server server;

    public ShutdownHookThread(Server server){
        this.server = server;
    }

    @Override
    public void run() {
        if(server != null && server.isOn()){
            this.setName("shutdown hook: " + server.getName());
            server.stop();
        }
        CoreLogger.info("停止服务");
    }
}
