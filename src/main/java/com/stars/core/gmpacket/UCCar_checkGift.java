package com.stars.core.gmpacket;

import com.stars.modules.tool.ToolManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/6/22.
 */
public class UCCar_checkGift extends GmPacketHandler {

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        Map<String, Object> data = new HashMap<>();
        String gameId = (String) args.get("gameId");
        String kaIdStr = (String) args.get("kaId");
        int kaId = Integer.parseInt(kaIdStr);
        if (ToolManager.UC_GIFT_MAP.containsKey(kaId)) {
            data.put("avaliable", true);
        } else {
            data.put("avaliable", false);
        }
        response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(data));
        return response.toString();
    }
}
