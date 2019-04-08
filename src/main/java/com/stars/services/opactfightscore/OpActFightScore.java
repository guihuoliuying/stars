package com.stars.services.opactfightscore;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-03-20 17:20
 */
public interface OpActFightScore extends Service, ActorService {

    @AsyncInvocation
    void view(long roleId);

    @AsyncInvocation
    void checkActivityFlowState(int activityId);
}
