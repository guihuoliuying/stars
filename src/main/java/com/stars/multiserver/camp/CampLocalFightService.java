package com.stars.multiserver.camp;

import com.stars.multiserver.camp.pojo.CampFightMatchInfo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by huwenjun on 2017/7/21.
 */
public interface CampLocalFightService extends Service, ActorService {
    @AsyncInvocation
    void startMatching(CampFightMatchInfo campFightMatchInfo);

    @AsyncInvocation
    void cancelMatching(CampFightMatchInfo campFightMatchInfo);

    @AsyncInvocation
    void matchFinish(int fromServerId, int fightServerId, long roleId);


    @AsyncInvocation
    void updateFightScore(int fromServerId, long roleId, int score);
}
