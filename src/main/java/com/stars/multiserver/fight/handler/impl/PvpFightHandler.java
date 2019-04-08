package com.stars.multiserver.fight.handler.impl;

import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.network.server.session.SessionManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.pvp.PvpFightInitData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/11/21.
 */
public class PvpFightHandler extends FightHandler {

    private long inviterId;
    private long inviteeId;

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args) {
        PvpFightInitData initData = (PvpFightInitData) args;
        FightRPC.pvpService().onFightCreationSucceeded(
                fromServerId, fightServerId, fightId, initData.getInviterId(), initData.getInviteeId());
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {

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
    public void init(Object args) {
        PvpFightInitData initData = (PvpFightInitData) args;
        inviterId = initData.getInviterId();
        inviteeId = initData.getInviteeId();
        RoleId2ActorIdManager.put(inviterId, fightId);
        RoleId2ActorIdManager.put(inviteeId, fightId);
        fighterIdSet.add(inviterId);
        fighterIdSet.add(inviteeId);
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
        ActorServer.getActorSystem().removeActor(fightId);
        SessionManager.remove(inviterId);
        SessionManager.remove(inviteeId);
        RoleId2ActorIdManager.remove(inviterId);
        RoleId2ActorIdManager.remove(inviteeId);
        RoleId2ActorIdManager.removeRoleId(inviterId);
        RoleId2ActorIdManager.removeRoleId(inviteeId);
    }

    @Override
    public void handleFighterExit(long roleId) {
        long winnerId = roleId == inviterId ? inviteeId : inviterId;
        long loserId = roleId == inviterId ? inviterId : inviteeId;
        ActorServer.getActorSystem().removeActor(fightId);
        actor.over();
        SessionManager.remove(inviterId);
        SessionManager.remove(inviterId);
        RoleId2ActorIdManager.remove(inviterId);
        RoleId2ActorIdManager.remove(inviteeId);
        RoleId2ActorIdManager.removeRoleId(inviterId);
        RoleId2ActorIdManager.removeRoleId(inviteeId);

        FightRPC.pvpService().finishPvp(fromServerId, inviterId, fightId, winnerId, loserId);
    }

    @Override
    public void handleFighterOffline(long roleId) {
        long winnerId = roleId == inviterId ? inviteeId : inviterId;
        long loserId = roleId == inviterId ? inviterId : inviteeId;
        ActorServer.getActorSystem().removeActor(fightId);
        actor.over();
        SessionManager.remove(inviterId);
        SessionManager.remove(inviterId);
        RoleId2ActorIdManager.remove(inviterId);
        RoleId2ActorIdManager.remove(inviteeId);
        RoleId2ActorIdManager.removeRoleId(inviterId);
        RoleId2ActorIdManager.removeRoleId(inviteeId);

        FightRPC.pvpService().finishPvp(fromServerId, inviterId, fightId, winnerId, loserId);
    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        FightRPC.pvpService().handleDead(fromServerId, inviterId, deadMap);
    }

    @Override
    public void handleDamage(long frameCount, Map<String, HashMap<String, Integer>> damageMap) {
        FightRPC.pvpService().handleDamage(fromServerId, inviterId, damageMap);
    }
}
