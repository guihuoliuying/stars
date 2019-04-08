package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class DeleteLoopNoticeGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            int serverId = Integer.parseInt((String) args.get("serverId"));
            // 公告ID
            int noticeId = Integer.parseInt((String) args.get("noticeId"));
            ServiceHelper.chatService().deleteLoopNotice(noticeId);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
