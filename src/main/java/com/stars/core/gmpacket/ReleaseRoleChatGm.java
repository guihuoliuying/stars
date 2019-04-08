package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class ReleaseRoleChatGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        long roleId = 0;
        int serverId = 0;
        if (args.containsKey("roleId")) {
            roleId = Long.valueOf((String) args.get("roleId"));
        }
        if (args.containsKey("serverId")) {
            serverId = Integer.parseInt((String) args.get("serverId"));
        }
        if (roleId == 0 || serverId == 0) {
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            return response.toString();
        }
        try {
            ServiceHelper.chatService().releaseForbidChater(roleId, serverId);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
