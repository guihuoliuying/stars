package com.stars.services.weeklyCharge;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;

/**
 * Created by chenxie on 2017/5/9.
 */
public interface WeeklyChargeService extends Service,ActorService{

    void closeActivity(int activityId);

}
