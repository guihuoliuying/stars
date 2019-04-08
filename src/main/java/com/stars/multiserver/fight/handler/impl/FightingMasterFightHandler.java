package com.stars.multiserver.fight.handler.impl;

import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.handler.FightHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/11/21.
 */
public class FightingMasterFightHandler  extends FightHandler{

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args) {
        FightRPC.fightingMasterService().createFightingCallback(fromServerId, fightId, true);
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        FightRPC.fightingMasterService().createFightingCallback(fromServerId, fightId, false);
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        FightRPC.fightingMasterService().addNewFighterCallback(fromServerId, fightId, true, entitySet);
    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {
        FightRPC.fightingMasterService().addNewFighterCallback(fromServerId, fightId, false, entitySet);
    }

    @Override
    public void onFightReadyFailed(int fightServerId, int fromServerId, String fightId) {
        FightRPC.fightingMasterService().noticeFightServerReadyCallback(fromServerId, fightId, false);
    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void init(Object args) {

    }

    @Override
    public void handleFightReady(int fightServerId, int fromServerId, String fightId) {
        FightRPC.fightingMasterService().noticeFightServerReadyCallback(fromServerId, fightId, true);
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {

    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        FightRPC.fightingMasterService().handleDead(fromServerId, fightId, deadMap);
    }

    @Override
    public void handleFighterOffline(long roleId) {
        FightRPC.fightingMasterService().handleOffline(fromServerId, fightId, roleId);
    }

    @Override
    public void handleFighterExit(long roleId) {
        handleFighterOffline(roleId);
    }

    @Override
    public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
        FightRPC.fightingMasterService().handleTimeOut(fromServerId, fightId, hpInfo);
    }
}
