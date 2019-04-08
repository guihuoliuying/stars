package com.stars.services.family.activities.treasure;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

/**
 * Created by chenkeyu on 2017/2/10 14:03
 */
public interface FamilyTreasureService extends Service, ActorService {

    @AsyncInvocation
    void online(long roleId, long familyId);

    @AsyncInvocation
    void start(int type, boolean startServer);

    @AsyncInvocation
    void end(int type, boolean startServer);

    @AsyncInvocation
    @DispatchAll
    void save();

    @AsyncInvocation
    void updateProcess(long familyId, int level, int step, boolean isOver);

    @AsyncInvocation
    void updateDamage(long familyId, long blood);

    @AsyncInvocation
    void offline(long familyId);

    @AsyncInvocation
    void leaveFamily(long roleId, long familyId);

    @AsyncInvocation
    void changeLevel(long roleId, long familyId, int level);

    @AsyncInvocation
    void changeFightScore(long roleId, long familyId, int fightScore);

    void newRank(long roleId, long familyId, int damage, int fightScore, int level);

    void dissolve(long familyId);

    long getFamilyTreasureDamage(long familyId);


    int getFamilyTreasureRank(long familyId);

    boolean isStart(int type);

    int startType();
}
