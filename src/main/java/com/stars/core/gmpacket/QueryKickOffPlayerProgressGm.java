package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;

/**
 * Created by wuyuxing on 2016/12/22.
 */
public class QueryKickOffPlayerProgressGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(KickOffPlayerGm.KICK_OFF_STATUS));
        return response.toString();
    }

}
