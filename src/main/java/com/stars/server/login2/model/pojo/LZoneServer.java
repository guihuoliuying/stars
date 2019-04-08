package com.stars.server.login2.model.pojo;

import java.net.InetSocketAddress;

/**
 * Created by zhaowenshuo on 2016/2/19.
 */
public class LZoneServer {

    private int id;
    private String name;
    private InetSocketAddress address;

    public LZoneServer(int id, String name, InetSocketAddress address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getIp() {
        return address.getHostString();
    }

    public int getPort() {
        return address.getPort();
    }
}
