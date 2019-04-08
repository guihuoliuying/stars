package com.stars.services.fightbase;

import com.stars.bootstrap.ServerManager;
import com.stars.modules.pk.packet.ClientUpdatePlayer;
import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.multiserver.fight.ClientOrders;
import com.stars.multiserver.fight.FightActor;
import com.stars.multiserver.fight.FightActorFactory;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.multiserver.fight.handler.FightHandlerFactory;
import com.stars.multiserver.fight.message.*;
import com.stars.multiserver.packet.NewFighterToFightActor;
import com.stars.multiserver.packet.StopFightActor;
import com.stars.network.PacketUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.ServiceSystem;
import com.stars.startup.FightStartup;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;
import io.netty.buffer.Unpooled;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * 尽可能需要双方/多方准备好了才开始战斗的情况
 * 1. 服务端：创建战斗，FightBaseService.createFight()
 * 2. 服务端：传输数据，FightBaseService.addFighter()
 * 3. 服务端：切换连接，MultiServerHelper.modifyConnectorRoute()；战斗服：FightHandler.onFigtherCome()
 * 4. 服务端：通知战斗服准备，
 * 5. 战斗服：下发数据，等待客户端预加载完成；客户端：预加载
 * 6. 战斗服/客户端：（全部）用户准备（倒计时-321）
 * 7. 战斗服/客户端：（全部）开始战斗
 * <p>
 * 可以中途加入战斗的情况
 * 1. 服务端：创建战斗，FightBaseService.createFight()
 * 2. 战斗服：开始战斗
 * ---- 加入战斗
 * 3. 服务端：传输数据，FightBaseService.addFighter()
 * 4. 服务端：切换连接，MultiServerHelper.modifyConnectorRoute()；战斗服：FightHandler.onFigtherCome()
 * 5. 战斗服：下发数据，等待客户端预加载完成；客户端：预加载
 * 6. 战斗服/客户端：（单个）用户准备（无敌状态，或在安全区）
 * 7. 客户端：（单个）开始战斗
 * <p>
 * Created by zhouyaohui on 2016/11/8.
 */
public class FightBaseServiceActor extends ServiceActor implements FightBaseService {

    private static Semaphore semaphore = new Semaphore(8);

    private String serviceName = "";

    public FightBaseServiceActor(int id) {
        serviceName = "fight-" + id;
    }

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(serviceName, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void createFight(int fightServerId, short handlerType, int fromServerId, String fightId,
                            ClientEnterFight enterPacket, Object args) {
        FightHandler handler = FightHandlerFactory.newInstance(handlerType);
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
//        if (semaphore.tryAcquire()) {
        if (true) {
            try {
                LogUtil.info("fightService|create|serviceName:{}|fightId:{}", serviceName, fightId);
                long st = System.currentTimeMillis();
                FightActor fightActor = null;
                if (handler == null || protoHandler == null) {
                    LogUtil.error("不存在Fight Handler Type: " + handlerType);
                    return;
                }
                try {
                    byte[] initData = packetToBytes(enterPacket);
                    handler.setHandlerType(handlerType);
                    handler.setFightId(fightId);
                    handler.setFightServerId(fightServerId);
                    handler.setFromServerId(fromServerId);
                    handler.init(args);
                    fightActor = FightActorFactory.newFightActor(fightId, handler, protoHandler, initData);
                    protoHandler.onFightCreationSucceeded(fightServerId, fromServerId, fightId, args);
                } catch (Exception e) {
                    if (fightActor != null) {
                        ActorServer.getActorSystem().removeActor(fightId);
                    }
                    protoHandler.onFightCreationFailed(fightServerId, fromServerId, fightId, args, e);
                }
                long et = System.currentTimeMillis();
                LogUtil.info("fightService|finishcreate|serviceName:{}|fightId:{}|elapsed:{}ms", serviceName, fightId, (et - st));
            } catch (Throwable t) {
                LogUtil.error(t.getMessage(), t);
            } finally {
//                semaphore.release();
            }
        } else {
            protoHandler.onFightCreationFailed(fightServerId, fromServerId, fightId, args, null);
        }
    }

    @Override
    public void createFight(int fightServerId, short handlerType, int fromServerId, String fightId, byte[] initData, Object args) {
        FightHandler handler = FightHandlerFactory.newInstance(handlerType);
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
//        if (semaphore.tryAcquire()) {
        if (true) {
            try {
                LogUtil.info("fightService|create|serviceName:{}|fightId:{}", serviceName, fightId);
                long st = System.currentTimeMillis();
                FightActor fightActor = null;
                if (handler == null || protoHandler == null) {
                    LogUtil.error("不存在Fight Handler Type: " + handlerType);
                    return;
                }
                try {
                    handler.setHandlerType(handlerType);
                    handler.setFightId(fightId);
                    handler.setFightServerId(fightServerId);
                    handler.setFromServerId(fromServerId);
                    handler.init(args);
                    fightActor = FightActorFactory.newFightActor(fightId, handler, protoHandler, initData);
                    protoHandler.onFightCreationSucceeded(fightServerId, fromServerId, fightId, args);
                } catch (Exception e) {
                    LogUtil.error("创建战斗异常", e);
                    if (fightActor != null) {
                        ActorServer.getActorSystem().removeActor(fightId);
                    }
                    protoHandler.onFightCreationFailed(fightServerId, fromServerId, fightId, args, e);
                }
                long et = System.currentTimeMillis();
                LogUtil.info("fightService|finishcreate|serviceName:{}|fightId:{}|elapsed:{}ms", serviceName, fightId, (et - st));
            } catch (Throwable t) {
                LogUtil.error(t.getMessage(), t);
            } finally {
//                semaphore.release();
            }
        } else {
            protoHandler.onFightCreationFailed(fightServerId, fromServerId, fightId, args, null);
        }
    }

    @Override
    public void readyFight(int fightServerId, short handlerType, int fromServerId, String fightId) {
        NoticeFightServerReady packet = new NoticeFightServerReady();
        packet.setFightId(fightId);
        packet.setServerId(fromServerId);
        packet.setData(ClientOrders.createReadyOrder());
        Actor actor = ActorServer.getActorSystem().getActor(fightId);
        actor.tell(packet, Actor.noSender);
    }

    @Override
    public void stopFight(int fightServerId, short handlerType, int fromServerId, String fightId) {
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        if (protoHandler == null) {
            LogUtil.error("不存在Fight Handler Type: " + handlerType);
            return;
        }
        try {
            Actor fightActor = ActorServer.getActorSystem().getActor(fightId);
            fightActor.tell(new StopFightActor(fightId), Actor.noSender);
        } catch (Exception e) {
            protoHandler.onFightStopFailed(fightServerId, fromServerId, fightId, e);
        }
    }

    @Override
    public void addFighter(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList) {
        HashSet<Long> fighterIdSet = new HashSet<>();
        FightHandler fightHandler = null;
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        List<String> roleList = new ArrayList<>();
        for (FighterEntity entity : entityList) {
            roleList.add(entity.getUniqueId());
        }
        LogUtil.info("familywar|fightServerId{},handlerType:{},fromServerId:{},fightId:{},roleList:{}", fightServerId, handlerType, fromServerId, fightId, roleList);
        if (protoHandler == null) {
            LogUtil.error("不存在Fight Handler Type: " + handlerType);
            return;
        }
        try {
            LogUtil.info("familywar|fightId:{}", fightId);
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            fightHandler = ((FightActor) actor).getFightHandler();
            fightHandler.handleAddFighter(entityList);
            Long longEntityUniqueId;
            for (FighterEntity entity : entityList) {
                if (entity.getFighterType() == FighterEntity.TYPE_SELF // fixme:
                        || entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    longEntityUniqueId = Long.parseLong(entity.getUniqueId());
                    RoleId2ActorIdManager.put(longEntityUniqueId, fightId);
                    fighterIdSet.add(longEntityUniqueId);
                }
            }
            LogUtil.info("addFighter|fighterIdSet {} ", fighterIdSet);
            actor.tell(createAddNewfighterToFightActor(fromServerId, fightId, fighterIdSet, entityList, true), Actor.noSender);
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
            protoHandler.onFighterAddingFailed(fightServerId, fromServerId, fightId, fighterIdSet, e);
        }
    }

    @Override
    public void addFighterNotSend(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList) {
        HashSet<Long> fighterIdSet = new HashSet<>();
        FightHandler fightHandler = null;
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        if (protoHandler == null) {
            LogUtil.error("不存在Fight Handler Type: " + handlerType);
            return;
        }
        try {
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            fightHandler = ((FightActor) actor).getFightHandler();
            Long longEntityUniqueId;
            for (FighterEntity entity : entityList) {
                if (entity.getFighterType() == FighterEntity.TYPE_SELF // fixme:
                        || entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
                    longEntityUniqueId = Long.parseLong(entity.getUniqueId());
                    RoleId2ActorIdManager.put(longEntityUniqueId, fightId);
                    fighterIdSet.add(longEntityUniqueId);
                }
            }
            actor.tell(createAddNewfighterToFightActor(fromServerId, fightId, fighterIdSet, entityList, false), Actor.noSender);
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
            protoHandler.onFighterAddingFailed(fightServerId, fromServerId, fightId, fighterIdSet, e);
        }
    }

    @Override
    public void addFighterSuccessSend(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList) {
//        HashSet<Long> fighterIdSet = new HashSet<>();
//        FightHandler fightHandler = null;
//        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
//        if (protoHandler == null) {
//            LogUtil.error("不存在Fight Handler Type: " + handlerType);
//            return;
//        }
//        try {
//            Actor actor = ActorServer.getActorSystem().getActor(fightId);
//            fightHandler = ((FightActor) actor).getFightHandler();
//            Long longEntityUniqueId;
//            for (FighterEntity entity : entityList) {
//                if (entity.getFighterType() == FighterEntity.TYPE_SELF // fixme:
//                        || entity.getFighterType() == FighterEntity.TYPE_PLAYER) {
//                    longEntityUniqueId = Long.parseLong(entity.getUniqueId());
//                    fighterIdSet.add(longEntityUniqueId);
//                }
//            }
//            actor.tell(createAddNewfighterToFightActor(fromServerId, fightId, fighterIdSet, entityList, true), Actor
//                    .noSender);
//        } catch (Exception e) {
//            LogUtil.error("add fight entry error", e);
//            protoHandler.onFighterAddingFailed(fightServerId, fromServerId, fightId, fighterIdSet, e);
//        }
    }

    @Override
    public void addMonster(int fightServerid, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList) {
        FightHandler fightHandler = null;
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        if (protoHandler == null) {
            LogUtil.error("不存在Fight Handler Type: " + handlerType);
            return;
        }

        try {
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            ClientUpdatePlayer packet = new ClientUpdatePlayer();
            for (FighterEntity entity : entityList) {
                LogUtil.info("camp:{}|id:{},entity:{}", entity.getCamp(), entity.getUniqueId(), entity.getAttribute());
            }
            packet.setNewFighter(entityList);
            actor.tell(new AddMonsterMessage(packet), Actor.noSender); // 包装成message防止客户端作弊
        } catch (Exception e) {
            LogUtil.error("add fight entry error", e);
        }


    }

    @Override
    public void removeFromFightActor(int fightServerId, short handlerType, int fromServerId, String fightId, List<Long> list) {
        try {
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            if (actor != null) {
                RemoveFromFightActor packet = new RemoveFromFightActor(list);
                actor.tell(packet, Actor.noSender);
            }
        } catch (Exception e) {
            LogUtil.error("removeFromFightActor error", e);
        }
    }

    @Override
    public void noticeFighterAddSuccess(int fightServerId, short handlerType, int fromServerId, String fightId, long roleId) {
        FightHandler fightHandler = null;
        FightHandler protoHandler = FightHandlerFactory.getProtoHandler(handlerType);
        if (protoHandler == null) {
            LogUtil.error("不存在Fight Handler Type: " + handlerType);
            return;
        }
        try {
            Actor actor = ActorServer.getActorSystem().getActor(fightId);
            if (actor != null) {
//                NoticeServerFighterAddSuccess success = new NoticeServerFighterAddSuccess(fromServerId, roleId);
//                actor.tell(success, Actor.noSender);
            }
        } catch (Exception e) {
            LogUtil.error("noticeFighterAddSuccess error", e);
        }
    }

    //    @Override
//    public void noticeFightServerReady(int fightServer, int formServer, NoticeFightServerReady ready) {
//        Actor actor = ActorServer.getActorSystem().getActor(ready.getFightId());
//        actor.tell(ready, Actor.noSender);
//    }

    private byte[] packetToBytes(Packet packet) {
        NewByteBuffer buffer = new NewByteBuffer(Unpooled.buffer());
        packet.writeToBuffer(buffer);
        byte[] bytes = new byte[buffer.getBuff().readableBytes()];
        buffer.getBuff().readBytes(bytes);
        buffer.getBuff().release();
        return bytes;
    }

    private AddNewfighterToFightActor createAddNewfighterToFightActor(int fromServerId, String fightId, Set<Long>
            fighterIdSet, List<FighterEntity> entityList, boolean notice) {
        NewFighterToFightActor o1 = new NewFighterToFightActor();
//        o1.setFightingId(fightId);
//        o1.setActorid(actorId);
        o1.setFightId(fightId);
        HashMap<Long, Byte> fighterIdOlMap = new HashMap<>();
        for (long id : fighterIdSet) {
            for (int i = 0, ilen = entityList.size(); i < ilen; i++) {
                if (entityList.get(i).getFighterType() != FighterEntity.TYPE_SELF &&
                        entityList.get(i).getFighterType() != FighterEntity.TYPE_PLAYER) {
                    continue;
                }
                if (Long.parseLong(entityList.get(i).getUniqueId()) == id) {
                    fighterIdOlMap.put(id, entityList.get(i).getState());
                    break;
                }
            }
        }
        o1.setFightersMap(fighterIdOlMap);
        LogUtil.info("AddNewfighterToFightActor|fighterIdOlMap:{}",fighterIdOlMap);
        // data
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.setNewFighter(entityList);
        o1.setData(packetToBytes(packet));

        AddNewfighterToFightActor o2 = new AddNewfighterToFightActor();
        o2.setServerId(fromServerId);
        o2.setNewer(o1);
        o2.setNoticeServer(notice);
        return o2;
    }

    @Override
    public void addServerOrder(int fightServerId, short handlerType, int fromServerId, String fightId, byte[] data) {
        Actor actor = ActorServer.getActorSystem().getActor(fightId);
        if (actor != null) {
            NoticeFightServerAddServerOrder packet = new NoticeFightServerAddServerOrder(fightId, fromServerId, data);
            actor.tell(packet, Actor.noSender);
        }
    }

    @Override
    public void addServerOrder(int fightServerId, short handlerType, int fromServerId, String fightId, ServerOrder order) {
        ClientUpdatePlayer packet = new ClientUpdatePlayer();
        packet.addOrder(order);
        Actor actor = ActorServer.getActorSystem().getActor(fightId);
        if (actor != null) {
            actor.tell(new NoticeFightServerAddServerOrder(fightId, fromServerId, PacketUtil.packetToBytes(packet)), Actor.noSender);
        }
    }

    @Override
    public void reloadProduct(int serverId) {
        try {
            FightStartup.loadProduct();
        } catch (Exception e) {
            LogUtil.info("战斗服重载产品数据失败,serverId={}", ServerManager.getServer().getConfig().getServerId());
            LogUtil.error("", e);
        }
    }
}
