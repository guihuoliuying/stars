package com.stars.server.main.gmpacket;

import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/10.
 */
public class GmPacketManager {
    public static Map<Integer, com.stars.server.main.gmpacket.GmPacketHandler> gmRequestMap = new HashMap<>();

    public static void regGmRequestHandler(int opType, Class<? extends com.stars.server.main.gmpacket.GmPacketHandler> clazz) {
        try {
            gmRequestMap.put(opType, clazz.newInstance());
        } catch (InstantiationException e) {
            com.stars.util.LogUtil.error("", e);
        } catch (IllegalAccessException e) {
            com.stars.util.LogUtil.error("", e);
        }
    }

    public static com.stars.server.main.gmpacket.GmPacketHandler getHandler(int opType) {
    	com.stars.util.LogUtil.error("opType:"+opType+"|gmRequestMap:size = "+gmRequestMap.size());
    	LogUtil.info(gmRequestMap.get(opType)==null?"true":"false");
        return (GmPacketHandler)gmRequestMap.get(opType);
    }
}
