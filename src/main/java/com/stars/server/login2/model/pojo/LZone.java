package com.stars.server.login2.model.pojo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaowenshuo on 2016/2/16.
 */
public class LZone {

    private int id;
    private String name;
    private List<LZoneServer> serverList;

    private AtomicInteger seq = new AtomicInteger(0);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LZoneServer> getServerList() {
        return serverList;
    }

    public void setServerList(List<LZoneServer> serverList) {
        this.serverList = serverList;
    }

    public void addServer(LZoneServer server) {
        serverList.add(server);
    }

}
