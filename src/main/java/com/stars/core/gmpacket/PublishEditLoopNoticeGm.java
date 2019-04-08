package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.cache.LoopNoticeCache;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class PublishEditLoopNoticeGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            // 公告ID,如果有该字段表明是编辑
            int noticeId = args.containsKey("noticeId") ? Integer.parseInt((String) args.get("noticeId")) : 0;
            // 标题
            String title = (String) args.get("title");
            // 公告内容
            String content = (String) args.get("content");
            // 开始时间,时间戳,单位秒
            long startTime = Long.parseLong((String) args.get("startTime"));
            // 结束时间,时间戳,单位秒
            long endTime = Long.parseLong((String) args.get("endTime"));
            // 循环间隔（秒）
            int cycleInterval = Integer.parseInt((String) args.get("cycleInterval"));
            // 优先级,数值越大,优先级越高
            int priority = Integer.parseInt((String) args.get("priority"));
            LoopNoticeCache loopNoticeCache = new LoopNoticeCache(title, content, startTime, endTime, cycleInterval, priority);
            if (noticeId == 0) {
                noticeId = ServiceHelper.chatService().newLoopNoticeId();
            }
            loopNoticeCache.setNoticeId(noticeId);
            ServiceHelper.chatService().publishEditLoopNotice(loopNoticeCache);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
