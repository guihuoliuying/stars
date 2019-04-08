package com.stars.services.newserverrank;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by gaopeidian on 2016/12/20.
 */
public interface NewServerRankService extends Service, ActorService {	
    void openActivity(int activityId);
    void closeActivity(int activityId);
    
    void getRewardInfo(int activityId , long roleId);

    @AsyncInvocation
    void getRankInfo(int activityId , long roleId);
}
