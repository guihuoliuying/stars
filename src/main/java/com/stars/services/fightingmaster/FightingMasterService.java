package com.stars.services.fightingmaster;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.packet.Packet;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/11/1.
 */
public interface FightingMasterService extends Service, ActorService {
    @AsyncInvocation
    void enterFightingMaster(int toServer, int fromServer, FighterEntity entry, FighterEntity buddy, int validMedalId, String familyName);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void dispatch(Packet packet);

    @AsyncInvocation
    void addNewFighterCallback(int serverId, String fightId, boolean result, Set<Long> fighters);

    @AsyncInvocation
    void createFightingCallback(int serverId, String fightId, boolean success);

    @AsyncInvocation
    void noticeFightServerReadyCallback(int serverId, String fightingId, boolean result);

    @AsyncInvocation
    void handleDead(int serverId, String fightId, Map<String, String> deadMap);

    @AsyncInvocation
    void rankAward();

    @AsyncInvocation
    void handleOffline(int fromServerId, String fightId, long roleId);

    @AsyncInvocation
    void handleTimeOut(int fromServerId, String fightId, HashMap<String, String> hpInfo);

    @AsyncInvocation
    void checkRankAward(int serverId, long roleId, int fromServer);

    @AsyncInvocation
    void rankAwardCallback(int fightingMasterServer, int serverId, Set<Long> awardSuccess);

    @AsyncInvocation
    void reSendAward();

    @AsyncInvocation
    void updateMaxFightingVal(int serverId, int fighting);

    @AsyncInvocation
    void getFightCount(int serverId, int fromServerId, long roleId);

    /**
     * 重载产品数据
     */
    @AsyncInvocation
    void reloadProduct(int serverId);

    @AsyncInvocation
    void updateRoleName(int fightingMasterServer, long id, String newName);
}
