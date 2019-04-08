package com.stars.multiserver.camp;

import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.network.server.packet.Packet;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by huwenjun on 2017/7/20.
 */
public interface CampRemoteFightService extends Service, ActorService {
    @AsyncInvocation
    void startMatching(int campServerId, CampFightMatchInfo campFightMatchInfo);

    @AsyncInvocation
    void cancelMatching(int campServerId, CampFightMatchInfo campFightMatchInfo);

    @AsyncInvocation
    void handleMatch();

    @AsyncInvocation
    void onFightCreationSuccessed(int fromServerId, int fightServerId, String fightId, boolean isOk, Object args);

    @AsyncInvocation
    void modifyConnect(String fightUid, int targetServerId);

    @AsyncInvocation
    void onFighterAddingFailed(int fromServerId, int fightServerId, String fightId, Set<Long> entitySet);

    @AsyncInvocation
    void handleFighterQuit(int fromServerId, String fightId, long roleId);

    @AsyncInvocation
    void handleFightDead(int fromServerId, String fightId, Map<String, String> deadMap);


    @AsyncInvocation
    void destroyRoom();

    @AsyncInvocation
    void handleContinueFight(int fromServerId, int fightServerId, String fightId, String fightUid);


    @AsyncInvocation
    void updateFighterExp(int fromServerId, int fightServerId, String fightId, HashMap<String, Integer> expMap);

    @AsyncInvocation
    void NotifyTheRoom(String battleId, Packet packet, String... excludeFightUid);

    @AsyncInvocation
    void onFighterAddingSucceeded(int fromServerId, int fightServerId, String fightId, Set<Long> entitySet);

    @AsyncInvocation
    void flushScoreRank(int fromServerId, int fightServerId, String fightId);

    @AsyncInvocation
    void updatePVPRoleNum(int campServerId, int roleNum);

    @AsyncInvocation
    void logRoomInfo();

    @AsyncInvocation
    void notifyRoleCurrentScore(String fightUid, Integer score);
}
