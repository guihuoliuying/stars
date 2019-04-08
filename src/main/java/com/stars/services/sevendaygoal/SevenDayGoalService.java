package com.stars.services.sevendaygoal;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by gaopeidian on 2016/12/19.
 */
public interface SevenDayGoalService extends Service, ActorService {
    /**
     * 保存
     */
    @AsyncInvocation
    void save();
    
    int getLeftRewardCount(int activityId , int goalId);
    
    byte getReward(int activityId , int goalId , long roleId);
    
    //void deleteActivityRecords(int activityId);
}
