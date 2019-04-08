package com.stars.multiserver.fight.handler.impl;

import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.packet.ServerCampFightPacket;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.handler.FightHandlerFactory;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightHandler;
import com.stars.multiserver.fight.handler.phasespk.PhasesPkFightManager;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.SessionManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.*;

/**
 * Created by huwenjun on 2017/7/21.
 */
public class CampFightHandler extends PhasesPkFightHandler {


    @Override
    public void onFightCreationSucceeded0(int fightServerId, int fromServerId, String fightId, Object args) {
        LogUtil.info("camp fight  create successful:{}", fightId);
        FightRPC.campRemoteFightService().onFightCreationSuccessed(fromServerId, fightServerId, fightId, true, args);
    }

    @Override
    public void onFightCreationFailed(int fightServerId, int fromServerId, String fightId, Object args, Throwable cause) {
        LogUtil.error("camp fight  create fail", cause);
        FightRPC.campRemoteFightService().onFightCreationSuccessed(fromServerId, fightServerId, fightId, false, args);
    }

    @Override
    public void onFighterAddingSucceeded(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet) {
        LogUtil.info("camp fight ：{} fighter add successful:{}", fightId, entitySet);
        FightRPC.campRemoteFightService().onFighterAddingSucceeded(fromServerId, fightServerId, fightId, entitySet);
    }

    @Override
    public void onFighterAddingFailed(int fightServerId, int fromServerId, String fightId, Set<Long> entitySet, Throwable cause) {
        LogUtil.error("campfight fighter add fail", cause);
        FightRPC.campRemoteFightService().onFighterAddingFailed(fromServerId, fightServerId, fightId, entitySet);
    }

    @Override
    public void onFightStopFailed(int fightServerId, int fromServerId, String fightId, Throwable cause) {
        LogUtil.error("camp fight stop fail ", cause);
    }


    @Override
    public void init0(Object obj) {
        registerPassThroughPacketType(ServerCampFightPacket.class);
    }

    public void handleAddFighter(List<FighterEntity> entityList) {
        for (FighterEntity fighterEntity : entityList) {
            entityMap.put(fighterEntity.getRoleId(), fighterEntity);
        }
    }

    public byte[] createEnterPacket(long roleId) {
        FighterEntity fighterEntity = entityMap.get(roleId);
        ClientEnterFight enterPacket = new ClientEnterFight();
        enterPacket.setFightType(SceneManager.SCENETYPE_CAMP_FIGHT);
        enterPacket.setStageId(CampManager.STAGE_ID_CAMP_FIGHT);
        List<FighterEntity> entityList = new ArrayList<>();
        entityList.add(fighterEntity);
        enterPacket.setFighterEntityList(entityList);
        StageinfoVo stageVo = SceneManager.getStageVo(CampManager.STAGE_ID_CAMP_FIGHT);
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        LogUtil.info("camp fight 动态阻挡数据Actor:{}", blockStatus);
        enterPacket.setBlockMap(stageVo.getDynamicBlockMap());
        enterPacket.addBlockStatusMap(blockStatus);
        byte[] bytes = PacketUtil.packetToBytes(enterPacket);
        return bytes;
    }

    @Override
    public void handleFightStop(int fightServerId, int fromServerId, String fightId) {
        LogUtil.info("camp fight room:{}  destory", fightId);

    }

    @Override
    public void handleFighterOffline(long roleId) {
        handleFighterExit(roleId);
    }


    @Override
    public void handleFighterExit(long roleId) {
        entityMap.remove(roleId);
        LogUtil.info("camp fight exit fighter:{}", roleId);
        fighterIdSet.remove(roleId);
        clear(roleId);
        FightRPC.campRemoteFightService().handleFighterQuit(fromServerId, fightId, roleId);

    }


    @Override
    public void handleDead(long frameCount, Map<String, String> deadMap) {
        for (Map.Entry<String, String> entry : deadMap.entrySet()) {
            entityMap.remove(Long.parseLong(entry.getKey()));
        }
        FightRPC.campRemoteFightService().handleFightDead(fromServerId, fightId, deadMap);
    }

    @Override
    public void handleMessage(Object message) {
        if (message instanceof ServerCampFightPacket) {
            ServerCampFightPacket serverCampFightPacket = (ServerCampFightPacket) message;
            switch (serverCampFightPacket.getSubType()) {
                case ServerCampFightPacket.REQ_CONTINUE_FIGHT: {
                    FightRPC.campRemoteFightService().handleContinueFight(fromServerId, fightServerId, fightId, serverCampFightPacket.getFightUid());
                }
                break;
                case ServerCampFightPacket.REQ_RANK_OF_THE_ROOM: {
                    FightRPC.campRemoteFightService().flushScoreRank(fromServerId, fightServerId, fightId);
                }
                break;
            }
        }
    }

    @Override
    public void handleLuaFrameData(long frameCount, LuaFrameData data, Object[] rawData) {
        HashMap<String, Integer> expMap = data.getExp();
        if (expMap != null && !expMap.isEmpty()) {
            for (Map.Entry<String, Integer> entry : expMap.entrySet()) {
                LogUtil.info("get exp roleid:{},exp:{}", entry.getKey(), entry.getValue());

            }
            FightRPC.campRemoteFightService().updateFighterExp(fromServerId, fightServerId, fightId, expMap);
        }
    }

    @Override
    public void handChangeConn(long roleId) {
        LogUtil.info("handChangeConn, roleId={}", roleId);
        arrivalFighterIdSet.add(roleId);
        if (!checkPhase(PhasesPkFightManager.PHASE_INITIAL)) {
            PacketManager.send(roleId, ScenePacketSet.C_ENTERFIGHT, 0, createEnterPacket(roleId)); // 客户端进入
            addFighter(entityMap.get(roleId)); // 只是为了同步数据
        }
    }

    public void handleFightStop0(int fightServerId, int fromServerId, String fightId) {
        try {
            handleFightStop(fightServerId, fromServerId, fightId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        ActorServer.getActorSystem().removeActor(fightId);
    }

    protected void addFighter(FighterEntity entity) {
        HashSet<Long> fighterIdSet = new HashSet<>();
        FightHandler fightHandler = null;
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        if (protoHandler == null) {
            LogUtil.error("PhasesPkFightHandler, 不存在Fight Handler Type: " + handlerType);
            return;
        }
        try {
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            fightHandler = ((FightActor) actor).getFightHandler();
            List<FighterEntity> entityList = new ArrayList<>();
            if (entity.getFighterType() == FighterEntity.TYPE_SELF // fixme:
                    || entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                RoleId2ActorIdManager.put(Long.parseLong(entity.getUniqueId()), fightId);
                fighterIdSet.add(Long.parseLong(entity.getUniqueId()));
                entityList.add(entity);
            }
            actor.tell(createAddNewfighterToFightActor(fromServerId, fightId, fighterIdSet, entityList), Actor.noSender);
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
            protoHandler.onFighterAddingFailed(fightServerId, fromServerId, fightId, fighterIdSet, e);
        }
    }

    @Override
    protected void finishInitialPhase() {
        setPhase(PhasesPkFightManager.PHASE_CLIENT_PREPARATION);
        for (Long roleId : arrivalFighterIdSet) {
            PacketManager.send(roleId, ScenePacketSet.C_ENTERFIGHT, 0, createEnterPacket(roleId)); // 客户端进入
            addFighter(entityMap.get(roleId));
            if (buddyEntityMap.containsKey(roleId)) {
                addMonster(buddyEntityMap.get(roleId));
            }
        }
        PhasesPkFightManager.addClientPreparationPhase(fightId, timeLimitOfClientPreparationPhase);
    }

    public void clear(long roleId) {
        LogUtil.info("role:{}清除战斗服相关连接数据:{}", roleId, fightId);
        SessionManager.remove(roleId);
        RoleId2ActorIdManager.removeRoleId(roleId);
        RoleId2ActorIdManager.remove(roleId);
    }
}
