package com.stars.server.login2.model.manager;

import com.stars.server.login2.sdk.core.LSdk;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LSdkManager {

    private static ConcurrentMap<Integer, LSdk> sdkMap = new ConcurrentHashMap<>();

    public static void register(int channelId, LSdk sdk) {
        Objects.requireNonNull(sdk, "渠道号为空");
        if (sdkMap.putIfAbsent(channelId, sdk) != null) {
            throw new IllegalArgumentException("渠道号重复");
        }
    }

    public static LSdk get(int channelId) {
        return sdkMap.get(channelId);
    }

}
