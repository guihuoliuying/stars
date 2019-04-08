package com.stars.multiserver.fight.handler.phasespk;

import com.stars.modules.chat.packet.ServerChatMessage;
import com.stars.modules.fightingmaster.packet.ServerFightReady;
import com.stars.modules.friend.packet.ServerBlacker;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.fight.ClientOrders;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.FightRPC;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.handler.FightHandlerFactory;
import com.stars.multiserver.fight.message.AddMonsterMessage;
import com.stars.multiserver.fight.message.AddNewfighterToFightActor;
import com.stars.multiserver.fight.message.NoticeFightServerReady;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.network.PacketUtil;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/11/30.
 */
public abstract class PhasesPkFightHandler extends FightHandler {

    private long timeLimitOfInitialPhase = 2000; // 最初延迟
    protected long timeLimitOfClientPreparationPhase = 3000; // 最大客户端准备时期

    protected int numOfFighter;
    private Map<Long, byte[]> enterPacketMap;
    protected Map<Long, FighterEntity> entityMap;
    protected Map<Long, FighterEntity> buddyEntityMap;

    private int phase; // 参考PhasesPkFightManager
    protected Set<Long> arrivalFighterIdSet;
    protected Set<Long> readyFighterIdSet;

    @Override
    public final void init(Object obj) {
        registerPassThroughPacketType(ServerFightReady.class);
        setPhase(PhasesPkFightManager.PHASE_INITIAL); // 初始阶段

        this.arrivalFighterIdSet = new HashSet<>();
        this.readyFighterIdSet = new HashSet<>();

        PhasesPkFightArgs args = (PhasesPkFightArgs) obj;
        this.numOfFighter = args.getNumOfFighter();
        this.enterPacketMap = args.getEnterPacketMap();
        this.entityMap = args.getEntityMap();
        this.buddyEntityMap = args.getBuddyEntityMap();
        if (this.buddyEntityMap == null) { //存在没携带伙伴信息的情况，避免空值异常
            this.buddyEntityMap = new HashMap<>();
        }
        this.timeLimitOfInitialPhase = args.getTimeLimitOfInitialPhase();
        this.timeLimitOfClientPreparationPhase = args.getTimeLimitOfClientPreparationPhase();

        for (Map.Entry<Long, FighterEntity> entry : entityMap.entrySet()) {
            long roleId = entry.getKey();
            FighterEntity fighterEntity = entry.getValue();
            if (fighterEntity.getFighterType() != FighterEntity.TYPE_ROBOT
                    && fighterEntity.getFighterType() != FighterEntity.TYPE_MONSTER) {
                RoleId2ActorIdManager.put(roleId, fightId);
                fighterIdSet.add(roleId);
            }
        }

        init0(args.getArgs0());
    }

    public abstract void init0(Object obj);

    @Override
    public final void onFightCreationSucceeded(int fightServerId, int fromServerId, String fightId, Object obj) {
        // 设置初始阶段时间
        PhasesPkFightArgs args = (PhasesPkFightArgs) obj;
        PhasesPkFightManager.addInitialPhase(fightId, args.getTimeLimitOfInitialPhase());
        // 创建成功回调
        onFightCreationSucceeded0(fightServerId, fromServerId, fightId, args);
    }

    public abstract void onFightCreationSucceeded0(int fightServerId, int fromServerId, String fightId, Object args);

    public void handleClientPreloadFinished(long roleId) {
    }


    public void handleUserPreparationFinished() {
    }


    public void handleFightStarted() {
    }

    @Override
    public void handChangeConn(long roleId) {
        LogUtil.info("handChangeConn, roleId={}", roleId);
        arrivalFighterIdSet.add(roleId);
        if (!checkPhase(PhasesPkFightManager.PHASE_INITIAL)) {
            PacketManager.send(roleId, ScenePacketSet.C_ENTERFIGHT, 0, enterPacketMap.get(roleId)); // 客户端进入
            addFighter(entityMap.get(roleId)); // 只是为了同步数据
        }
    }

    protected void finishInitialPhase() {
        setPhase(PhasesPkFightManager.PHASE_CLIENT_PREPARATION);
        for (Long roleId : arrivalFighterIdSet) {
            PacketManager.send(roleId, ScenePacketSet.C_ENTERFIGHT, 0, enterPacketMap.get(roleId)); // 客户端进入
            addFighter(entityMap.get(roleId));
            if (buddyEntityMap.containsKey(roleId)) {
                addMonster(buddyEntityMap.get(roleId));
            }
        }
        PhasesPkFightManager.addClientPreparationPhase(fightId, timeLimitOfClientPreparationPhase);
    }

    void finishClientPreparationPhase() {
        setPhase(PhasesPkFightManager.PHASE_USER_PREPARATION);
        // 对不在线的玩家也要强行加进去
        for (Map.Entry<Long, FighterEntity> entry : entityMap.entrySet()) {
            long roleId = entry.getKey();
            FighterEntity entity = entry.getValue();
            if (!arrivalFighterIdSet.contains(roleId)
                    && (entity.getFighterType() == FighterEntity.TYPE_SELF || entity.getFighterType() == FighterEntity.TYPE_PLAYER)) { // 未加载完的玩家/怪物/AI
                addFighter(entity);
                if (buddyEntityMap.containsKey(roleId)) {
                    addMonster(buddyEntityMap.get(roleId));
                }
            }
            if (entity.getFighterType() == FighterEntity.TYPE_ROBOT
                    || entity.getFighterType() == FighterEntity.TYPE_MONSTER) {
                addMonster(entity);
            }
        }

        NoticeFightServerReady readyMessage = new NoticeFightServerReady();
        readyMessage.setFightId(fightId);
        readyMessage.setServerId(fromServerId);
        readyMessage.setData(ClientOrders.createReadyOrder());
        actor.tell(readyMessage, Actor.noSender);

        handleFightStarted();
    }

    void finishUserPreparationPhase() {
        setPhase(PhasesPkFightManager.PHASE_FIGHT);
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
//                RoleId2ActorIdManager.put(Long.parseLong(entity.getUniqueId()), fightId);
//                fighterIdSet.add(Long.parseLong(entity.getUniqueId()));
                entityList.add(entity);
            }
            actor.tell(createAddNewfighterToFightActor(fromServerId, fightId, fighterIdSet, entityList), Actor.noSender);
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
            protoHandler.onFighterAddingFailed(fightServerId, fromServerId, fightId, fighterIdSet, e);
        }
    }

    public AddNewfighterToFightActor createAddNewfighterToFightActor(int fromServerId, String fightId,
                                                                     Set<Long> fightIdSet, List<FighterEntity> entityList) {
        NewFighterToFightActor o1 = new NewFighterToFightActor();
        o1.setFightId(fightId);
        HashMap<Long, Byte> fightIdMap = new HashMap<>();
        for (Long id : fightIdSet) {
            fightIdMap.put(id, (byte) 0);
        }
        o1.setFightersMap(fightIdMap);
        // data
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.setNewFighter(entityList);
        o1.setData(PacketUtil.packetToBytes(packet));

        AddNewfighterToFightActor o2 = new AddNewfighterToFightActor();
        o2.setServerId(fromServerId);
        o2.setNewer(o1);
        return o2;
    }

    public void addMonster(FighterEntity entity) {
        try {
            List<FighterEntity> list = new ArrayList<>();
            list.add(entity);
            ClientUpdatePlayer packet = new ClientUpdatePlayer();
            packet.setNewFighter(list);
            actor.tell(new AddMonsterMessage(packet), Actor.noSender); // 包装成message防止客户端作弊
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
        }
    }

    @Override
    public void handleMessage(Object message) {
        if (message instanceof ServerFightReady) {
            ServerFightReady packet = (ServerFightReady) message;
            long roleId = packet.getRoleId();
            if (arrivalFighterIdSet.contains(roleId)) {
                readyFighterIdSet.add(roleId);
                try {
                    handleClientPreloadFinished(roleId);
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
            }
            if (readyFighterIdSet.size() >= numOfFighter) {
                if (PhasesPkFightManager.removeClientPreparationPhase(fightId)) {
                    finishClientPreparationPhase();
                }
            }
            return;
        }

        if (message instanceof ServerChatMessage) {
            ServerChatMessage msg = (ServerChatMessage) message;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(fromServerId, msg.getRoleId(), msg);
            return;
        }
        if (message instanceof ServerBlacker) {
            ServerBlacker msg = (ServerBlacker) message;
            msg.setPlayer(null);
            msg.setSession(null);
            FightRPC.roleService().exec(fromServerId, msg.getRoleId(), msg);
            return;
        }
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public boolean checkPhase(int expectedPhase) {
        return this.phase == expectedPhase;
    }
}
