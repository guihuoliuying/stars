package com.stars.services.localservice;

import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.tool.packet.InnerSendAwardPacket;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/7.
 */
public class LocalServiceActor extends ServiceActor implements LocalService {

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.LocalService, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void sendAward(int serverId, long roleId, short eventType, int emailTemplateId, Map<Integer, Integer> toolMap) {
        Player player = PlayerSystem.get(roleId);
        if (player != null) {
            try {
                Packet packet = new InnerSendAwardPacket(eventType, toolMap);
                GameSession session = SessionManager.getSessionMap().get(roleId);
                packet.setSession(session);
                player.tell(new InnerSendAwardPacket(eventType, toolMap), Actor.noSender);
                return;
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
        // 如果找不到玩家，或者告诉玩家消息失败时
        ServiceHelper.emailService().sendToSingle(roleId, emailTemplateId, 0L, "", toolMap);
    }

    @Override
    public void sendFightingMasterAward(int serverId, Map<Long, String> awardMap) {
    }
}
