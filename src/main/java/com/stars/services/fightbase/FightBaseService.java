package com.stars.services.fightbase;

import com.stars.modules.pk.packet.ServerOrder;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterFight;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.Dispatch;

import java.util.List;

/**
 * Created by zhouyaohui on 2016/11/8.
 */
public interface FightBaseService extends Service, ActorService {

    // ---- 业务结合修改(start
//    @Deprecated
    @AsyncInvocation
    @Dispatch(index = 3)
    // fightId
    void createFight(int fightServerId, short handlerType, int fromServerId, String fightId, ClientEnterFight enterPacket, Object args);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void createFight(int fightServerId, short handlerType, int fromServerId, String fightId, byte[] initData, Object args);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void readyFight(int fightServerId, short handlerType, int fromServerId, String fightId);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void stopFight(int fightServerId, short handlerType, int fromServerId, String fightId);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void addFighter(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList);

    @AsyncInvocation
    @Dispatch(index = 3)
    void addFighterNotSend(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList);

    @AsyncInvocation
    @Dispatch(index = 3)
    void addFighterSuccessSend(int fightServerId, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void addServerOrder(int fightServerId, short handlerType, int fromServerId, String fightId, byte[] data);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void addServerOrder(int fightServerId, short handlerType, int fromServerId, String fightId, ServerOrder order);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void addMonster(int fightServerid, short handlerType, int fromServerId, String fightId, List<FighterEntity> entityList);

    @AsyncInvocation
    @Dispatch(index = 3)
        // fightId
    void removeFromFightActor(int fightServerId, short handlerType, int fromServerId, String fightId, List<Long> list);

    @AsyncInvocation
    @Dispatch(index = 3)
    void noticeFighterAddSuccess(int fightServerId, short handlerType, int fromServerId, String fightId, long roleId);
    // ---- 业务结合修改(end
//
//    @AsyncInvocation
//    void noticeFightServerReady(int fightServer, int formServer, NoticeFightServerReady ready);

    /**
     * 重载产品数据
     */
    @AsyncInvocation
    void reloadProduct(int serverId);
}
