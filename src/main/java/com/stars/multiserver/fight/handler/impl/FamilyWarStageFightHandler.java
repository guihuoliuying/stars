package com.stars.multiserver.fight.handler.impl;

import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.familyactivities.war.packet.ui.ServerFamilyWarSafeSceneEnter;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.knockout.fight.stage.FamilyWarStageFightArgs;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.network.server.session.SessionManager;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FamilyWarStageFightHandler extends FightHandler {

    private Map<Long, byte[]> newFighters;
    private Map<Long, Integer> roleWarType;
    private String battleId;

    private int mainServerId;

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args) {
        FamilyWarStageFightArgs data = (FamilyWarStageFightArgs) args;
        LogUtil.info("familywar|战斗创建成功 fightServerId:{},fromServerId:{},fightId:{},mainServerId:{},battleId:{}", fightServerId, fromServerId, fightId, mainServerId, data.getBattleId());
        int warType = getWarType(data);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onStageFightCreationSucceeded(fromServerId, fightServerId, data.getBattleId(), fightId);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().onStageFightCreationSucceeded(fromServerId, mainServerId, fightServerId, data.getBattleId(), fightId);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().onStageFightCreationSucceeded(fromServerId, mainServerId, fightServerId, data.getBattleId(), fightId);
        }
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        FamilyWarStageFightArgs data = (FamilyWarStageFightArgs) args;
        int warType = getWarType(data);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onFightCreateFail(fromServerId, fightServerId, battleId, fightId, FamilyWarConst.WarTypeStage);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {

        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {

        }
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        int warType = getWarType();
        LogUtil.info("familywar|匹配场 warType:{}, fightServerId:{},fromServerId:{},fightId:{},entitySet:{}", warType, fightServerId, fromServerId, fightId, entitySet);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onStageFighterAddingSucceeded(fromServerId, fightServerId, battleId, fightId, entitySet);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().onStageFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, entitySet);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().onStageFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, entitySet);
        }
    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {

    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void init(Object args) {
        FamilyWarStageFightArgs data = (FamilyWarStageFightArgs) args;
        this.battleId = data.getBattleId();
        this.mainServerId = data.getMainServerId();
        for (Long roleId : data.getRoleIds()) {
            RoleId2ActorIdManager.put(roleId, fightId);
            fighterIdSet.add(roleId);
        }
        newFighters = new HashMap<>();
        this.roleWarType = data.getRoleWarType();
        LogUtil.info("familywar|roleWarType:{}", roleWarType);
        registerPassThroughPacketType(ServerChatMessage.class);
        registerPassThroughPacketType(ServerBlacker.class);
    }

    @Override
    public void handChangeConn(long roleId) {
        LogUtil.info("FamilyWarStageFightHandler|handChangeConn, roleId={}", roleId);
        byte[] data = newFighters.get(roleId);
        if (data != null) {
            actor.addServerOrder(data);
            newFighters.remove(roleId);
        }
        int warType = getWarType();
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onClientPreloadFinished(fromServerId, fightServerId, battleId, fightId, roleId);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().onClientPreloadFinished(fromServerId, mainServerId, fightServerId, battleId, fightId, roleId);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().onClientPreloadFinished(fromServerId, mainServerId, fightServerId, battleId, fightId, roleId);
        }
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
        for (Long roleId : fighterIdSet) {
            MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        }
    }

    // fixme: 临时措施
    @Override
    public void handleFighterExit(long roleId) {
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerExitFight());
    }

    @Override
    public void handleFighterExitToFamilySafeScene(long roleId) {
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerFamilyWarSafeSceneEnter());
        fighterIdSet.remove(roleId);
    }

    @Override
    public void handleFighterOffline(long roleId) {
        int warType = getWarType();
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_STAGE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_STAGE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_STAGE_FIGHT);
        }
        removeFighterId(roleId);
        SessionManager.remove(roleId);
        RoleId2ActorIdManager.removeRoleId(roleId);
        RoleId2ActorIdManager.remove(roleId);
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {

    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        int warType = getWarType();
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleStageFightDead(fromServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleStageFightDead(fromServerId, mainServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleStageFightDead(fromServerId, mainServerId, battleId, fightId, deadMap);
        }
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
            if (kvp.getValue() == (byte) 0) {//初次进入
                addFighterId(fighterId);
                newFighters.put(fighterId, newers.getData());
            } else {//复活
                actor.addServerOrder(newers.getData());
            }
        }
        onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
    }

    @Override
    public void handleMessage(Object message) {
        if (message instanceof ServerChatMessage) {
            ServerChatMessage msg = (ServerChatMessage) message;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(mainServerId, msg.getRoleId(), msg);
            return;
        }
        if (message instanceof ServerBlacker) {
            ServerBlacker msg = (ServerBlacker) message;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(mainServerId, msg.getRoleId(), msg);
            return;
        }
    }

    private int getWarType(long roleId) {
        return roleWarType.get(roleId);
    }

    private int getWarType(FamilyWarStageFightArgs data) {
        int warType = 0;
        for (int type : data.getRoleWarType().values()) {
            warType = type;
            break;
        }
        return warType;
    }

    private int getWarType() {
        int warType = 0;
        for (int type : roleWarType.values()) {
            warType = type;
            break;
        }
        return warType;
    }
}
