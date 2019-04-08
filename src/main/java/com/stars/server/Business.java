package com.stars.server;

import com.stars.network.server.packet.Packet;

/**
 * Created by zd on 2015/3/13.
 */
public interface Business {

    /**
     * 此方法在游戏启动时被调用，用来初始化业务层配置和加载数据（如Packet和CacheTable的实现类）
     */
    void init() throws Exception;

    /**
     * 此方法在游戏关闭时被调用，用来清理业务层所占用的资源
     */
    void clear();

    /**
     * 请求从IO层分派到业务层的接口（业务层决定请求是分到线程池或是Actor）
     * 具体流程：
     *   1. 拆包，得到packet
     *   2. Business.dispatch(packet)
     * @param packet 请求
     */
    void dispatch(Packet packet);

}
