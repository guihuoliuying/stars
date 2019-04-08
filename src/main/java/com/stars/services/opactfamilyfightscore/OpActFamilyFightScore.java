package com.stars.services.opactfamilyfightscore;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-03-20 11:40
 */
public interface OpActFamilyFightScore extends Service, ActorService{

    @AsyncInvocation
    void view(long roleId);

    @AsyncInvocation
    void checkActivityFlowState(int activityId);
}
