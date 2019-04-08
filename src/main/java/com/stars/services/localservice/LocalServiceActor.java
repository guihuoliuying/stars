package com.stars.services.localservice;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.rank.RankManager;
import com.stars.modules.tool.packet.InnerSendAwardPacket;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Set<Long> awardSuccess = new HashSet<>();
        try {
            for (Map.Entry<Long, String> entry : awardMap.entrySet()) {
                String[] award = entry.getValue().split("[+]");
                List<RankAwardVo> list = RankManager.getRankAward(Integer.valueOf(award[0]));
                for (RankAwardVo vo : list) {
                    if (vo.isInSectionRange(Integer.valueOf(award[1]))) {
                        if (award[1].equals("101")) {
                            ServiceHelper.emailService().sendToSingle(entry.getKey(), vo.getEmail(), 0l, "巅峰对决奖励", vo.getRewardMap(), "100+");
                        } else {
                            ServiceHelper.emailService().sendToSingle(entry.getKey(), vo.getEmail(), 0l, "巅峰对决奖励", vo.getRewardMap(), award[1]);
                        }
                        break;
                    }
                }
                awardSuccess.add(entry.getKey());
            }
        } catch (Exception e) {
            LogUtil.error("fighting master award error.", e);
        } finally {
            MainRpcHelper.fightingMasterService().rankAwardCallback(MultiServerHelper.getFightingMasterServer(),
                    MultiServerHelper.getServerId(), awardSuccess);
        }
    }
}
