package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.gm.GmManager;
import com.stars.modules.gm.event.GmRedpointEvent;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by wuyuxing on 2017/3/30.
 */
public class SendGmRedpoint extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        long roleId = 0;
        GmPacketResponse response = null;
        Object roleIdObject = args.containsKey("roleId");
        if (roleIdObject != null) {
            roleId = Long.parseLong((String) args.get("roleId"));
        } else {
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            return response.toString();
        }

        try {
            GmManager.GM_MESSAGE_REDPOINTS.put(roleId, 1);
            Player player = PlayerSystem.get(roleId);
            if (player != null) {
                ServiceHelper.roleService().notice(roleId, new GmRedpointEvent());
            }
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
            return response.toString();
        }
        response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        return response.toString();
    }
}
