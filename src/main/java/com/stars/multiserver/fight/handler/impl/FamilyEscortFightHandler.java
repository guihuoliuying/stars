package com.stars.multiserver.fight.handler.impl;

import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/4/19.
 */
public class FamilyEscortFightHandler extends PhasesPkFightHandler {

    @Override
    public void init0(Object obj) {
        registerPassThroughPacketType(ServerChatMessage.class);
        registerPassThroughPacketType(ServerBlacker.class);
    }

    @Override
    public void onFightCreationSucceeded0(int fightServerId, int fromServerId, String fightId, Object args) {
        FightRPC.familyEscortService().rpcOnFightCreated(fromServerId, fightId, true);
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        FightRPC.familyEscortService().rpcOnFightCreated(fromServerId, fightId, false);
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {

    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {

    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
        for (long roleId : fighterIdSet) {
            MultiServerHelper.modifyConnectorRoute(roleId, fromServerId);
        }
    }

    @Override
    public void handleFighterOffline(long roleId) {
        actor.stopFight(fightServerId, fromServerId);
        FightRPC.familyEscortService().rpcOnFightEnd(fromServerId, fightServerId, fightId, roleId);
    }

    @Override
    public void handleFighterExit(long roleId) {
        handleFighterOffline(roleId);
    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        for (String uid : deadMap.keySet()) {
            if (!uid.startsWith("b")) {
                actor.stopFight(fightServerId, fromServerId);
                FightRPC.familyEscortService().rpcOnFightEnd(fromServerId, fightServerId, fightId, toLong(uid));
                break;
            }
        }
    }

    @Override
    public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
        double minRatio = 1.0;
        long loserRoleId = 0;
        for (Map.Entry<String, String> entry : hpInfo.entrySet()) {
            Long uid = toLong(entry.getKey());
            if (fighterIdSet.contains(uid)) {
                String[] info = entry.getValue().split("[+]");
                double ratio = Float.valueOf(info[1]) / Float.valueOf(info[0]);
                if (ratio <= minRatio) {
                    minRatio = ratio;
                    loserRoleId = uid;
                }
            }
        }
        actor.stopFight(fightServerId, fromServerId);
        FightRPC.familyEscortService().rpcOnFightEnd(fromServerId, fightServerId, fightId, loserRoleId);
    }
}
