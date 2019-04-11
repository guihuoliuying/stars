package com.stars.multiserver.fight.handler.impl;

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
    public void onFightReadyFailed(int fightServerId, int fromServerId, String fightId) {
    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void init(Object args) {

    }

    @Override
    public void handleFightReady(int fightServerId, int fromServerId, String fightId) {
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {

    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
    }

    @Override
    public void handleFighterOffline(long roleId) {
    }

    @Override
    public void handleFighterExit(long roleId) {
        handleFighterOffline(roleId);
    }

    @Override
    public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
    }
}
