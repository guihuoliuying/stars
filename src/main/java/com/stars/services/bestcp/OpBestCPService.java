package com.stars.services.bestcp;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by huwenjun on 2017/5/20.
 */
public interface OpBestCPService extends Service, ActorService {
    @AsyncInvocation
    void openActivity(int activityId);

    @AsyncInvocation
    void closeActivity(int activityId);

    @AsyncInvocation
    void dailyReset();

    @AsyncInvocation
    void vote(long roleid, int cpId);
}
