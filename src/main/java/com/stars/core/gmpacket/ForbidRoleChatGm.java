package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class ForbidRoleChatGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        long roleId = 0;
        int serverId = 0;
        long expireTime = 0;
        String reason = "";
        if (args.containsKey("roleId")) {
            roleId = Long.valueOf((String) args.get("roleId"));
        }
        if (args.containsKey("serverId")) {
            serverId = Integer.parseInt((String) args.get("serverId"));
        }
        if (args.containsKey("expiresTime")) {// 运营传过来单位是秒,转成毫秒
            expireTime = Long.parseLong((String) args.get("expiresTime")) * 1000L;
        }
        if (args.containsKey("blockReason")) {
            reason = (String) args.get("blockReason");
        }
        if (roleId == 0 || serverId == 0 || expireTime == 0 || "".equals(reason)) {
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            return response.toString();
        }
        try {
            ServiceHelper.chatService().forbidChater(roleId, serverId, expireTime, reason);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
