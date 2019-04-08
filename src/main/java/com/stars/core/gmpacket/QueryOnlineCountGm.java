package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.network.server.session.GameSession;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.core.actor.AbstractActor;

import java.util.HashMap;

/**
 * 运维GM接口
 * 查看在线人数
 * Created by liuyuheng on 2016/12/12.
 */
public class QueryOnlineCountGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        int onlineCount = 0;
        for (AbstractActor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                GameSession gameSession = ((Player) actor).session();
                if (gameSession != null && gameSession.isActive()) {
                    onlineCount++;
                }
            }
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 1, resultToJson(onlineCount));
        return response.toString();
    }
}
