package com.stars.services.newofflinepvp;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-03-08 15:44
 */
public interface NewOfflinePvpService extends ActorService, Service {

    @AsyncInvocation
    void dealExitFight(byte victoryOrDefeat, long otherRoleId, long selfRoleId, int level, int jobId, String roleName, int fightScore);

    @AsyncInvocation
    void openOfflinePvp(long roleId, int level, int jobId, String roleName, int fightScore);

    @AsyncInvocation
    void sendRankList(long roleId);

    @AsyncInvocation
    void sendBattleReport(long roleId);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void match(long roleId, int maxRank, int remainFightCount, int remainBuyCount, byte first);

    @AsyncInvocation
    void sendRankAward();

    @AsyncInvocation
    void changeRoleLevel(long roleId, int level);
    @AsyncInvocation
    void changeRoleName(long roleId, String newName);

    @AsyncInvocation
    void changeRoleJob(long roleId, int jobId);

    @AsyncInvocation
    void changeRoleFightScore(long roleId, int fightScore);

    @AsyncInvocation
    void closeOfflinePvp();

    @AsyncInvocation
    void openOfflinePvp();

    int getRank(long roleId);

    boolean lockRole(long selfRoleId, long otherRoleId);

    @AsyncInvocation
    void unLockRole(long selfRoleId, long otherRoleId);
}
