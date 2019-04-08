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
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightArgs;
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

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/11/22.
 */
public class FamilyWarEliteFightHandler extends FightHandler {

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
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        FamilyWarEliteFightArgs data = (FamilyWarEliteFightArgs) args;
        int warType = getWarType(data);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().onFightCreateFail(fromServerId, fightServerId, battleId, fightId, FamilyWarConst.WarTypeElite);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {

        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {

        }
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        Iterator<Long> iterator = entitySet.iterator();
        if (iterator.hasNext()) {
            Long roleId = iterator.next();
            byte camp = 1;
            if (campMap.containsKey(roleId)) {
                camp = campMap.get(roleId);
            }
            int mainServerId = getMainServerId(camp);
            int warType = roleWarType.get(roleId);
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FightRPC.familyWarLocalService().onFighterAddingSucceeded(fromServerId, fightServerId, battleId, fightId, camp, roleId);
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                FightRPC.familyWarQualifyingService().onFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, camp, roleId);
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                FightRPC.familyWarRemoteService().onFighterAddingSucceeded(fromServerId, mainServerId, fightServerId, battleId, fightId, camp, roleId);
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
        FamilyWarEliteFightArgs args0 = (FamilyWarEliteFightArgs) args;
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
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, getMainServerId(campMap.get(roleId)), fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        }
    }

    // fixme: 临时措施
    @Override
    public void handleFighterExit(long roleId) {
        byte camp = campMap.get(roleId);
        int mainServerId = getMainServerId(camp);
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerExitFight());
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        }
        fighterIdSet.remove(roleId);
    }

    @Override
    public void handleFighterExitToFamilySafeScene(long roleId) {
        LogUtil.info("camp:{}", campMap);
        byte camp = campMap.get(roleId);
        int mainServerId = getMainServerId(camp);
        MultiServerHelper.modifyConnectorRoute(roleId, mainServerId);
        FightRPC.roleService().exec(mainServerId, roleId, new ServerFamilyWarSafeSceneEnter());
        int warType = roleWarType.get(roleId);
        if (warType == FamilyWarConst.W_TYPE_LOCAL) {
            FightRPC.familyWarLocalService().handleFighterQuit(fromServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleFighterQuit(fromServerId, mainServerId, fightServerId, fightId, battleId, roleId, FightConst.T_FAMILY_WAR_ELITE_FIGHT);
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
                LogUtil.info("familywar|newFihgter:{}", fighterId);
                addFighterId(fighterId);
                newFighters.put(fighterId, newers.getData());
            }
        }
        actor.addServerOrder(newers.getData());
        LogUtil.info("familywar|handleNewFighter");
        if (aNewfighterToFightActor.isNoticeServer()) {
            LogUtil.info("familywar|handleNewFighter,notice");
            onFighterAddingSucceeded(MultiServerHelper.getServerId(), aNewfighterToFightActor.getServerId(), fightId, set);
        }
    }

    @Override
    public void handleNoticeFighterAddSuceess(int serverId, long roleId) {
        Set<Long> set = new HashSet<>();
        set.add(roleId);
        LogUtil.info("familywar|handleNoticeFighterAddSuceess,notice");
        onFighterAddingSucceeded(MultiServerHelper.getServerId(), serverId, fightId, set);
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
            FightRPC.familyWarLocalService().handleEliteFightDamage(fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarLocalService().handleEliteFightDead(fromServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
            FightRPC.familyWarQualifyingService().handleEliteFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarQualifyingService().handleEliteFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
        } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
            FightRPC.familyWarRemoteService().handleEliteFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            FightRPC.familyWarRemoteService().handleEliteFightDead(fromServerId, fromServerId, battleId, fightId, deadMap);
        }
        accumulatedDamageMap.clear();
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
        if (frameCount % 30 == 0 && !accumulatedDamageMap.isEmpty()) {
            int warType = getWarType();
            if (warType == FamilyWarConst.W_TYPE_LOCAL) {
                FightRPC.familyWarLocalService().handleEliteFightDamage(fromServerId, battleId, fightId, accumulatedDamageMap);
            } else if (warType == FamilyWarConst.W_TYPE_QUALIFYING) {
                FightRPC.familyWarQualifyingService().handleEliteFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
            } else if (warType == FamilyWarConst.W_TYPE_REMOTE) {
                FightRPC.familyWarRemoteService().handleEliteFightDamage(fromServerId, fromServerId, battleId, fightId, accumulatedDamageMap);
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

    private int getWarType(FamilyWarEliteFightArgs data) {
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
}
