package com.stars.core.gmpacket;

import com.stars.core.player.PlayerSystem;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;


/**
 * 运维GM接口
 * 设置单区人数上限
 * Created by chenkeyu on 2016/12/17.
 */
public class SingleAreaNumUpLimitGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        int num = Integer.parseInt((String)args.get("value"));
        PlayerSystem.setActorCount(num);
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC,1,resultToJson(0));
        return response.toString();
    }
}
