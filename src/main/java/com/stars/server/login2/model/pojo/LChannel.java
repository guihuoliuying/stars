package com.stars.server.login2.model.pojo;

/**
 * Created by zhaowenshuo on 2016/1/29.
 */
public class LChannel {

    private int id;              // 渠道ID
    private String name;

    private String cpId;                // 渠道分配给游戏的cpID
    private String cpAppId;             // 渠道分配给游戏的appID
    private String cpAppKey;            // 渠道分配给游戏的AppKey
    private String cpAppSecret;         // 渠道分配给游戏的AppSecret

    public LChannel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

}
