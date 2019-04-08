package com.stars.multiserver.daregod;

import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public interface DareGodService extends Service, ActorService {

    @AsyncInvocation
    void onLine(int serverId, int mainServerId, long roleId, String roleName, int fashionId, int fightScore, int jobId);

    @AsyncInvocation
    void view(int serverId, int mainServerId, long roleId, String roleName, int fightScore, int jobId, int vipLv);

    @AsyncInvocation
    void viewRank(int serverId, int mainServerId, long roleId);

    @AsyncInvocation
    void dealExitOrFinishScene(int serverId, int mainServerId, long roleId, ClientStageFinish csf, long damae, int fightType);

    @AsyncInvocation
    void getTargetAward(int serverId, int mainServerId, long roleId, int targetId);

    @AsyncInvocation
    void buyTimes(int serverId, int mainServerId, long roleId, int vipLv, int buyTime);

    @AsyncInvocation
    void enterFight(int serverId, int mainServerId, long roleId);

    @AsyncInvocation
    void delFightTime(int serverId, int mainServerId, long roleId, int time);

    @AsyncInvocation
    void updateFightState(int serverId, boolean state);

    @AsyncInvocation
    void onDaliyReset(int serverId);

    @AsyncInvocation
    void updateRoleName(int serverId, long roleId, String roleName);

    @AsyncInvocation
    void updateFightScore(int serverId, long roleId, int fightScore);

    @AsyncInvocation
    void updateFashionId(int serverId, long roleId, int fashionId);

    @AsyncInvocation
    void updateJobId(int serverId, long roleId, int newJobId);

    @AsyncInvocation
    void registerServer(int serverId, int mainServerId);

    @AsyncInvocation
    void save();
}
