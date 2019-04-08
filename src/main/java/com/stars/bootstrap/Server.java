package com.stars.bootstrap;

/**
 * Created by jx on 2015/2/27.
 */
public interface Server {

    void start() throws Exception; // 启动服务

    void stop(); // 停止服务

    String getName(); // 获得启动的服务名

    void setOn();

    boolean isOn(); // 服务是否已启动

    void setOff();

    boolean isOff(); // 服务是否已停止
}
