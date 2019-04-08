package com.stars.core.gmpacket;

import com.stars.multiserver.MultiServerHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.cache.LoopNoticeCache;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class QueryLoopNoticeGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            int serverId = Integer.parseInt((String) args.get("serverId"));
            List<Map> list = new ArrayList<>();
            Map<Integer, LoopNoticeCache> loopNoticeCacheMap = ServiceHelper.chatService().queryLoopNotice();
            if (!StringUtil.isEmpty(loopNoticeCacheMap)) {
                for (LoopNoticeCache cache : loopNoticeCacheMap.values()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("noticeId", cache.getNoticeId());
                    map.put("serverId", MultiServerHelper.getServerId());
                    map.put("title", cache.getTitle());
                    map.put("content", cache.getContent());
                    map.put("startTime", cache.getStartTime());
                    map.put("endTime", cache.getEndTime());
                    map.put("cycleInterval", cache.getCycleInterval());
                    map.put("priority", cache.getPriority());
                    list.add(map);
                }
            }
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(list));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
