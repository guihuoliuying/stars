package com.stars.core.gmpacket;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.HashMap;

/**
 * Created by zhanghaizhen on 2017/8/1.
 */
public class AnnounceGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        String message = (String) args.get("value");
        for(int i = 0; i <= 10; i ++) {
            ServiceHelper.chatService().announce(message);//发送全服公告
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(0));
        return response.toString();
    }
}
