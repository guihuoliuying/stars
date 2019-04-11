package com.stars.multiserver.fight.handler.impl;

import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.packet.NewFighterToFightActor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TPGFightHandler extends FightHandler {
    private Map<Long, byte[]> newFighters = new HashMap<>();

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId,
                                         String fightId, Object args) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId,
                                      String fightId, Object args, Throwable cause) {
        // 创建失败要通知业务处理一下

    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId,
                                         String fightId, Set<Long> entitySet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId,
                                      String fightId, Set<Long> entitySet, Throwable cause) {
        // 加人失败要通知业务处理一下
    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId,
                                  String fightId, Throwable cause) {
        // 停止失败需要处理吗?

    }

    @Override
    public void init(Object args) {
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId,
                                String fightId) {

    }

    @Override
    public void handleFighterOffline(long roleId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handNewFighter(AddNewfighterToFightActor aNewfighterToFightActor) {
        NewFighterToFightActor newers = aNewfighterToFightActor.getNewer();
        //中途加入的玩家需要处理
        Map<Long, Byte> fighterOlMap = newers.getFightersMap();
        Set<Long> set = new HashSet<>();
        Long fighterId;
        //注意：这里应该只有一个元素的;
        for (Map.Entry<Long, Byte> kvp : fighterOlMap.entrySet()) {
            fighterId = kvp.getKey();
            set.add(fighterId);
            //注意: 离线的不需要加入这里了;
            addFighterId(fighterId);
            newFighters.put(fighterId, newers.getData());
            //注意: 离线的才发，在线的不需要发; 将NewFighterToFightActor的fighters改为map,记录是否在线;
//            actor.addServerOrder(newers.getData());
        }
    }

    @Override
    public void handleMessage(Object message) {
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
    }
}
