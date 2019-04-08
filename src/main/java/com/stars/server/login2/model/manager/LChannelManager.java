package com.stars.server.login2.model.manager;

import com.stars.server.login2.model.pojo.LChannel;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LChannelManager {

    private static ConcurrentMap<Integer, LChannel> channelMap = new ConcurrentHashMap<>();

    public static void register(int channelId, LChannel channel) {
        Objects.requireNonNull(channel, "渠道为空");
        if (channelMap.putIfAbsent(channelId, channel) != null) {
            throw new IllegalArgumentException("渠道号重复");
        }
    }

    public static LChannel get(int channelId) {
        return channelMap.get(channelId);
    }

}
