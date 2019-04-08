package com.stars.core.module;

import com.stars.network.server.packet.Packet;

/**
 * 用来模块内的访问控制（主要用于登录过程，因为登录过程是异步，要防止客户端乱发包），第一个模块必须实现
 * Created by zhaowenshuo on 2016/1/8.
 */
public interface Guard {

    /**
     * 判断数据包是否可以访问
     *
     * @param packet 数据包
     * @return 可以访问返回true；不可访问返回false
     */
    boolean canAccess(Packet packet);

    void onCallAccess(int packetId);

}
