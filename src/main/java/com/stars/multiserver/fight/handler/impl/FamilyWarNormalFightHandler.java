package com.stars.multiserver.fight.handler.impl;

import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightBlock;
import com.stars.modules.familyactivities.war.packet.ClientFamilyWarBattleFightDirect;
import com.stars.modules.familyactivities.war.packet.ServerFamilyWarBattleFightDirect;
import com.stars.modules.familyactivities.war.packet.ServerFamilyWarBattleFightRevive;
import com.stars.modules.familyactivities.war.packet.ui.ServerFamilyWarSafeSceneEnter;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFightArgs;
import com.stars.multiserver.fight.ClientOrders;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightConst;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.fight.message.NoticeFightServerReady;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.core.actor.Actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/11/29.
 */
//public class FamilyWarNormalFightHandler extends PhasesPkFightHandler {
public class FamilyWarNormalFightHandler extends FightHandler {

    private boolean isLocal;
    private Map<Long, byte[]> newFighters = new HashMap<>();

    private String battleId;
    private Map<Long, Byte> campMap = new HashMap<>();
    private Map<Long, Integer> roleWarType = new HashMap<>();
    private int camp1MainServerId;
    private int camp2MainServerId;
    private Map<String, HashMap<String, Integer>> accumulatedDamageMap = new HashMap<>();
    private long createTimeStamp;
    private boolean canceledDynamicBlock = false;

    @Override
    public void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object args) {
        finishClientPreparationPhase(fightId);
        FamilyWarNormalFightArgs data = (FamilyWarNormalFightArgs) args;
        int warType = getWarType(data);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onNormalFightCreationSucceeded(fromServerId, fightServerId, data.getBattleId(), fightId);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().onNormalFightCreationSucceeded(fromServerId, fromServerId, fightServerId, data.getBattleId(), fightId);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().onNormalFightCreationSucceeded(fromServerId, fromServerId, fightServerId, data.getBattleId(), fightId);
        }
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        FamilyWarNormalFightArgs data = (FamilyWarNormalFightArgs) args;
        int warType = getWarType(data);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onFightCreateFail(fromServerId, fightServerId, data.getBattleId(), fightId, FamilyWarConst.WarTypeElite);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {

        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {

        }
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        LogUtil.info("handler|entitySet:{}", entitySet);
        for (long roleId : entitySet) {
            LogUtil.info("handler|onFighterAddingSucceeded| roleId: {}", roleId);
            byte camp = 1;
            if (campMap.containsKey(roleId)) {
                camp = campMap.get(roleId);
            }
            int mainServerId = getMainServerId(camp);
            int warType = roleWarType.get(roleId);
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FightRPC.familyWarLocalService().onNormalFighterAddingSucceeded(fromServerId, fightServerId, battleId, fightId, camp, roleId);
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                FightRPC.familyWarQualifyingService().onNormalFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, camp, roleId);
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                FightRPC.familyWarRemoteService().onNormalFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, camp, roleId);
            }
        }
    }

    private int getMainServerId(byte camp) {
        int mainServerId;
        if (camp == FamilyWarConst.K_CAMP1) {
            mainServerId = camp1MainServerId;
        } else {
            mainServerId = camp2MainServerId;
        }
        return mainServerId;
    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {

    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {

    }

    @Override
    public void init(Object args) {
        FamilyWarNormalFightArgs args0 = (FamilyWarNormalFightArgs) args;
        this.battleId = args0.getBattleId();
        this.createTimeStamp = args0.getCreateTimestamp();
        this.campMap = args0.getCampMap();
        this.camp1MainServerId = args0.getCamp1MainServerId();
        this.camp2MainServerId = args0.getCamp2MainServerId();
        this.roleWarType = args0.getRoleWarType();
        registerPassThroughPacketType(ServerChatMessage.class);
        registerPassThroughPacketType(ServerBlacker.class);
        registerPassThroughPacketType(ServerFamilyWarBattleFightDirect.class);
        registerPassThroughPacketType(ServerFamilyWarBattleFightRevive.class);
        registerPassThroughPacketType(ServerFamilyWarSafeSceneEnter.class);
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
        for (Map.Entry<Long, Byte> entry : campMap.entrySet()) {
            if (entry.getValue() == FamilyWarConst.K_CAMP1) {
                MultiServerHelper.modifyConnectorRoute(entry.getKey(), camp1MainServerId);
            } else if (entry.getValue() == FamilyWarConst.K_CAMP2) {
                MultiServerHelper.modifyConnectorRoute(entry.getKey(), camp2MainServerId);
            }
        }
    }

    @Override
    public void handleFighterOffline(long roleId) {
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        }
    }

    // fixme: 临时措施
    @Override
    public void handleFighterExit(long roleId) {
        byte camp = campMap.get(roleId);
        int mainServerId = getMainServerId(camp);
        LogUtil.info("退出|handleFighterExit|roleId:{},toServerId:{}", roleId, mainServerId);
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerExitFight());
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        }
        fighterIdSet.remove(roleId);
    }

    @Override
    public void handleFighterExitToFamilySafeScene(long roleId) {
        byte camp = campMap.get(roleId);
        int mainServerId = getMainServerId(camp);
        LogUtil.info("退出|handleFighterExitToFamilySafeScene|roleId:{},toServerId:{}", roleId, mainServerId);
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerFamilyWarSafeSceneEnter());
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_NORMAL_FIGHT);
        }
        fighterIdSet.remove(roleId);
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
                LogUtil.info("familywar|normal|newFihgter:{}", fighterId);
                addFighterId(fighterId);
                newFighters.put(fighterId, newers.getData());
            }
        }
        actor.addServerOrder(newers.getData());
        LogUtil.info("familywar|normal|handleNewFighter");
        if (aNewfighterToFightActor.isNoticeServer()) {
            LogUtil.info("familywar|handleNewFighter,notice");
            onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
        }
    }

    @Override
    public void handChangeConn(long roleId) {
        LogUtil.info("handChangeConn, roleId={}", roleId);
        byte[] data = newFighters.get(roleId);
        if (data != null) {
            actor.addServerOrder(data);
            newFighters.remove(roleId);
        }
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onClientPreloadFinished(fromServerId, fightServerId, battleId, fightId, roleId);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().onClientPreloadFinished(fromServerId, fromServerId, fightServerId, battleId, fightId, roleId);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().onClientPreloadFinished(fromServerId, fromServerId, fightServerId, battleId, fightId, roleId);
        }
    }


    /* 战斗结果处理 */
    @Override
    public void handleDamage(long frameCount, Map<String, HashMap<String, Integer>> damageMap) {
        // 不每帧返回
        for (Map.Entry<String, HashMap<String, Integer>> entry : damageMap.entrySet()) {
            HashMap<String, Integer> map = accumulatedDamageMap.get(entry.getKey());
            if (map == null) {
                accumulatedDamageMap.put(entry.getKey(), entry.getValue());
            } else {
                MapUtil.add(map, entry.getValue());
            }
        }
    }

    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        int warType = getWarType();
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleNormalFightDamage(fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarLocalService().handleNormalFightDead(fromServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarQualifyingService().handleNormalFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarRemoteService().handleNormalFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
        }
        accumulatedDamageMap.clear();
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
        if (frameCount % 30 == 0 && !accumulatedDamageMap.isEmpty()) {
            int warType = getWarType();
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FightRPC.familyWarLocalService().handleNormalFightDamage(fromServerId, battleId, fightId, accumulatedDamageMap);
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                FightRPC.familyWarQualifyingService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                FightRPC.familyWarRemoteService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            }
            accumulatedDamageMap.clear();
        }
        if (frameCount % 30 == 0 && !canceledDynamicBlock) {
            if (System.currentTimeMillis() >= createTimeStamp + FamilyActWarManager.DYNAMIC_BLOCK_TIME * 1000) {
                ClientFamilyWarBattleFightBlock packet = createCancelBlockPacket();
                if (packet != null) {
                    for (Long fighterId : fighterIdSet) {
                        PacketManager.send(fighterId, packet);
                    }
                    canceledDynamicBlock = true;
                }
            }
        }
    }

    /**
     * 关闭所有动态阻挡
     */
    public ClientFamilyWarBattleFightBlock createCancelBlockPacket() {
        StageinfoVo stageVo = SceneManager.getStageVo(FamilyActWarManager.stageIdOfEliteFight);
        if (stageVo == null) return null;
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
        }
        ClientFamilyWarBattleFightBlock blockPacket = new ClientFamilyWarBattleFightBlock(blockStatus);
        return blockPacket;
    }

    void finishClientPreparationPhase(String fightId) {
        Actor actor = ActorServer.getActorSystem().getActor(fightId);
        if (actor != null) {
            NoticeFightServerReady readyMessage = new NoticeFightServerReady();
            readyMessage.setFightId(fightId);
            readyMessage.setServerId(fromServerId);
            readyMessage.setData(ClientOrders.createReadyOrder());
            actor.tell(readyMessage, Actor.noSender);
            LogUtil.info("actor ready");
        } else {
            LogUtil.info("no actor");
        }
    }

    @Override
    public void handleMessage(Object message) {
        Packet packet = (Packet) message;
        if (packet instanceof ServerChatMessage) {
            ServerChatMessage msg = (ServerChatMessage) packet;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(getMainServerId(campMap.get(packet.getRoleId())), packet.getRoleId(), msg);
            return;
        }

        if (packet instanceof ServerBlacker) {
            ServerBlacker msg = (ServerBlacker) packet;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(getMainServerId(campMap.get(packet.getRoleId())), packet.getRoleId(), msg);
            return;
        }

        if (packet instanceof ServerFamilyWarBattleFightDirect) {
            ServerFamilyWarBattleFightDirect req = (ServerFamilyWarBattleFightDirect) packet;
            ClientFamilyWarBattleFightDirect resp = new ClientFamilyWarBattleFightDirect(
                    Long.toString(req.getRoleId()), req.getTowerCamp(), req.getTowerType());
            Byte camp = campMap.get(req.getRoleId());
            if (camp != null) {
                for (Map.Entry<Long, Byte> entry : campMap.entrySet()) {
                    if (entry.getValue() == camp) {
                        PacketManager.send(entry.getKey(), resp);
                    }
                }
            }
        }
        if (packet instanceof ServerFamilyWarBattleFightRevive) {
            ServerFamilyWarBattleFightRevive req = (ServerFamilyWarBattleFightRevive) packet;
            int warType = getWarType();
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FightRPC.familyWarLocalService().revive(fromServerId, fightServerId, battleId, fightId, req.getRoleId(), req.getReqType());
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                FightRPC.familyWarQualifyingService().revive(fromServerId, fromServerId, fightServerId, battleId, fightId, req.getRoleId(), req.getReqType());
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                FightRPC.familyWarRemoteService().revive(fromServerId, fromServerId, fightServerId, battleId, fightId, req.getRoleId(), req.getReqType());
            }
        }
    }

    private int getWarType(long roleId) {
        return roleWarType.get(roleId);
    }

    private int getWarType(FamilyWarNormalFightArgs data) {
        int warType = 0;
        for (Integer type : data.getRoleWarType().values()) {
            warType = type;
            break;
        }
        return warType;
    }

    private int getWarType() {
        int warType = 0;
        for (Integer type : roleWarType.values()) {
            warType = type;
            break;
        }
        return warType;
    }

//    private String battleId;
//    private Map<Long, Integer> roleWarType;
//    private Map<Long, Byte> campMap;
//    private int camp1MainServerId;
//    private int camp2MainServerId;
//
//    @Override
//    public void onFightCreationSucceeded0(int fightServerId, int fromServerId, String fightId, Object args) {
//        FamilyWarNormalFightArgs args0 = (FamilyWarNormalFightArgs) ((PhasesPkFightArgs) args).getArgs0();
//        int warType = getWarType(args0);
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().onNormalFightCreationSucceeded(fromServerId, fightServerId, args0.getBattleId(), fightId);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().onNormalFightCreationSucceeded(fromServerId, fromServerId, fightServerId, args0.getBattleId(), fightId);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().onNormalFightCreationSucceeded(fromServerId, fromServerId, fightServerId, args0.getBattleId(), fightId);
//        }
//    }
//
//    @Override
//    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
//        FamilyWarNormalFightArgs args0 = (FamilyWarNormalFightArgs) ((PhasesPkFightArgs) args).getArgs0();
//        int warType = getWarType(args0);
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().onFightCreateFail(fromServerId, fightServerId, battleId, fightId, FamilyWarConst.WarTypeNormal);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//
//        }
//    }
//
//    @Override
//    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
//
//    }
//
//    @Override
//    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {
//
//    }
//
//    @Override
//    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {
//
//    }
//
//    @Override
//    public void init0(Object args) {
//        FamilyWarNormalFightArgs args0 = (FamilyWarNormalFightArgs) args;
//        battleId = args0.getBattleId();
//        camp1MainServerId = args0.getCamp1MainServerId();
//        camp2MainServerId = args0.getCamp2MainServerId();
//        campMap = args0.getCampMap();
//        roleWarType = args0.getRoleWarType();
//        registerPassThroughPacketType(ServerChatMessage.class);
//        registerPassThroughPacketType(ServerBlacker.class);
//    }
//
//    @Override
//    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
//        for (Long fighterId : fighterIdSet) {
//            if (campMap.get(fighterId) == FamilyWarConst.K_CAMP1) {
//                MultiServerHelper.modifyConnectorRoute(fighterId, camp1MainServerId);
//            }
//            if (campMap.get(fighterId) == FamilyWarConst.K_CAMP2) {
//                MultiServerHelper.modifyConnectorRoute(fighterId, camp2MainServerId);
//            }
//        }
//    }
//
//    @Override
//    public void handleFighterOffline(long roleId) {
//        int warType = roleWarType.get(roleId);
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, battleId, roleId);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), battleId, roleId);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), battleId, roleId);
//        }
//    }
//
//    void finishClientPreparationPhase() {
//        NoticeFightServerReady readyMessage = new NoticeFightServerReady();
//        readyMessage.setFightId(fightId);
//        readyMessage.setServerId(fromServerId);
//        readyMessage.setData(ClientOrders.createReadyOrder());
//        actor.tell(readyMessage, Actor.noSender);
//    }
//
//    @Override
//    public void handleClientPreloadFinished(long roleId) {
//        int warType = getWarType();
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().onClientPreloadFinished(fromServerId, fightServerId, battleId, fightId, roleId);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().onClientPreloadFinished(fromServerId, getMainServerId(roleId), fightServerId, battleId, fightId, roleId);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().onClientPreloadFinished(fromServerId, getMainServerId(roleId), fightServerId, battleId, fightId, roleId);
//        }
//    }
//
//    @Override
//    public void handleFightStarted() {
//        int warType = getWarType();
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().onNormalFightStarted(fromServerId, fightServerId, battleId, fightId);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().onNormalFightStarted(fromServerId, fromServerId, fightServerId, battleId, fightId);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().onNormalFightStarted(fromServerId, fromServerId, fightServerId, battleId, fightId);
//        }
//
//    }
//
//    // fixme: 临时措施
//    @Override
//    public void handleFighterExit(long roleId) {
//        MultiServerHelper.modifyConnectorRoute(roleId, getMainServerId(roleId));
//        FightRPC.roleService().exec(getMainServerId(roleId), roleId, new ServerExitFight());
//        fighterIdSet.remove(roleId);
//    }
//
//    @Override
//    public void handleFighterExitToFamilySafeScene(long roleId) {
//        MultiServerHelper.modifyConnectorRoute(roleId, getMainServerId(roleId));
//        FightRPC.roleService().exec(getMainServerId(roleId), roleId, new ServerFamilyWarSafeSceneEnter());
//        fighterIdSet.remove(roleId);
//    }
//
//    @Override
//    public void handleDamage(long frameCount, Map<String, HashMap<String, Integer>> damageMap) {
//        int warType = getWarType();
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().handleNormalFightDamage(fromServerId, battleId, fightId, damageMap);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, damageMap);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().handleNormalFightDamage(fromServerId, fromServerId, battleId, fightId, damageMap);
//        }
//    }
//
//    @Override
//    public void handleDead(long frameCount, Map<String, String> deadMap) {
//        for (String victimUid : deadMap.keySet()) {
//            MultiServerHelper.modifyConnectorRoute(Long.parseLong(victimUid), getMainServerId(victimUid));
//        }
//        int warType = getWarType();
//        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
//            FightRPC.familyWarLocalService().handleNormalFightDead(fromServerId, battleId, fightId, deadMap);
//        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
//            FightRPC.familyWarQualifyingService().handleNormalFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
//        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
//            FightRPC.familyWarRemoteService().handleNormalFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
//        }
//    }
//
//    @Override
//    public void handleMessage(Object message) {
//        if (message instanceof ServerFightReady) {
//            ServerFightReady packet = (ServerFightReady) message;
//            long roleId = packet.getRoleId();
//            if (arrivalFighterIdSet.contains(roleId)) {
//                readyFighterIdSet.add(roleId);
//                try {
//                    handleClientPreloadFinished(roleId);
//                } catch (Exception e) {
//                    LogUtil.error("", e);
//                }
//            }
//            if (readyFighterIdSet.size() >= numOfFighter) {
//                if (PhasesPkFightManager.removeClientPreparationPhase(fightId)) {
//                    finishClientPreparationPhase();
//                }
//            }
//            return;
//        }
//
//        if (message instanceof ServerChatMessage) {
//            ServerChatMessage msg = (ServerChatMessage) message;
//            msg.setPlayer(null);
//            msg.setSession(null);
//            FightRPC.roleService().exec(getMainServerId(msg.getRoleId()), msg.getRoleId(), msg);
//            return;
//        }
//        if (message instanceof ServerBlacker) {
//            ServerBlacker msg = (ServerBlacker) message;
//            msg.setPlayer(null);
//            msg.setSession(null);
//            FightRPC.roleService().exec(getMainServerId(msg.getRoleId()), msg.getRoleId(), msg);
//            return;
//        }
//    }
//
//    private int getMainServerId(String fighterUid) {
//        long roleId = Long.parseLong(fighterUid);
//        if (campMap.containsKey(roleId)) {
//            switch (campMap.get(roleId)) {
//                case FamilyWarConst.K_CAMP1:
//                    return camp1MainServerId;
//                case FamilyWarConst.K_CAMP2:
//                    return camp2MainServerId;
//            }
//        }
//        return 0;
//    }
//
//    private int getMainServerId(long roleId) {
//        if (campMap.containsKey(roleId)) {
//            switch (campMap.get(roleId)) {
//                case FamilyWarConst.K_CAMP1:
//                    return camp1MainServerId;
//                case FamilyWarConst.K_CAMP2:
//                    return camp2MainServerId;
//            }
//        }
//        return 0;
//    }
//
//    private int getWarType(long roleId) {
//        return roleWarType.get(roleId);
//    }
//
//    private int getWarType(FamilyWarNormalFightArgs args) {
//        int warType = 0;
//        for (Integer type : args.getRoleWarType().values()) {
//            warType = type;
//            break;
//        }
//        return warType;
//    }
//
//    private int getWarType() {
//        int warType = 0;
//        for (Integer type : roleWarType.values()) {
//            warType = type;
//            break;
//        }
//        return warType;
//    }
}
