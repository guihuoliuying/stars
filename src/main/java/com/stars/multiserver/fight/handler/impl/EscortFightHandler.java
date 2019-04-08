package com.stars.multiserver.fight.handler.impl;

import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.network.server.session.SessionManager;
import com.stars.services.escort.EscortFightInitData;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EscortFightHandler extends FightHandler {

    private Map<Long, byte[]> newFighters;

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args) {
        EscortFightInitData data = (EscortFightInitData) args;
        FightRPC.escortService().onFightCreationSucceeded(fromServerId,fightServerId,fightId,data.getEscortRoleIds());
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {

    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        FightRPC.escortService().onFighterAddingSucceeded(fromServerId, fightServerId, fightId, entitySet);
    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {

    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void init(Object args) {
        EscortFightInitData data = (EscortFightInitData) args;
        for(Long roleId:data.getEscortRoleIds()){
            RoleId2ActorIdManager.put(roleId, fightId);
            fighterIdSet.add(roleId);
        }

        newFighters = new HashMap<Long, byte[]>();
    }

    @Override
    public void handChangeConn(long roleId) {
        byte[] data = newFighters.get(roleId);
        if (data != null) {
            actor.addServerOrder(data);
            newFighters.remove(roleId);
        }
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {

    }

    @Override
    public void handleFighterOffline(long roleId) {
        FightRPC.escortService().handleOffline(fromServerId, fightId, roleId);

        removeFighterId(roleId);
        SessionManager.remove(roleId);
        RoleId2ActorIdManager.removeRoleId(roleId);
        RoleId2ActorIdManager.remove(roleId);
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
        if(data == null || (StringUtil.isEmpty(data.getCargoPosition()) &&
                 StringUtil.isEmpty(data.getDeadPos()))) return;
        FightRPC.escortService().doFightFramData(fromServerId,fightId,data);
    }

    @Override
    public void handleTimeOut(long frameCount, HashMap<String, String> hpInfo) {
        FightRPC.escortService().handleTimeOut(fromServerId, fightId, hpInfo);
    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        FightRPC.escortService().handleDead(fromServerId, fightId, deadMap);
    }

    @Override
    public void handNewFighter(AddNewfighterToFightActor aNewfighterToFightActor) {
        NewFighterToFightActor newers = aNewfighterToFightActor.getNewer();
        //中途加入的玩家需要处理
        Map<Long, Byte> fighterOlMap = newers.getFightersMap();
        Set<Long> set = new HashSet<>();
        Long fighterId;
        //注意：这里应该只有一个元素的;
        for(Map.Entry<Long, Byte> kvp : fighterOlMap.entrySet()){
            fighterId = kvp.getKey();
            set.add(fighterId);
            if(kvp.getValue() == (byte)0){//初次进入
                addFighterId(fighterId);
                newFighters.put(fighterId, newers.getData());
            }else{//复活
                actor.addServerOrder(newers.getData());
            }
        }
        onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
    }

}
