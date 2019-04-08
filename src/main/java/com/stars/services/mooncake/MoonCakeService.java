package com.stars.services.mooncake;

import com.stars.modules.mooncake.packet.ClientMoonCake;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-09-21.
 */
public interface MoonCakeService extends Service, ActorService {

    @AsyncInvocation
    void initMoonCake();

    @AsyncInvocation
    void closeMoonCake();

    @AsyncInvocation
    void view(long roleId, ClientMoonCake packet);

    @AsyncInvocation
    void viewRank(long roleId);

    @AsyncInvocation
    void updateMaxWeeklyScore(long roleId, int score/*, String roleName, int fightScore*/);

    @AsyncInvocation
    void removeFromRank(long roleId);

    @AsyncInvocation
    void updateRoleName(long roleId, String roleName);

    @AsyncInvocation
    void updateFightScore(long roleId, int fightScore);
}
