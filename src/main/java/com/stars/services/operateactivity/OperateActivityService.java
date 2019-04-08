package com.stars.services.operateactivity;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by gaopeidian on 2016/12/26.
 */
public interface OperateActivityService extends Service, ActorService {
	/**
     * 保存
     */
//    @AsyncInvocation
//    void save();
	
    @AsyncInvocation
    void checkOpActivities(boolean isInit);
    
//    @AsyncInvocation
//    void dailyReset();
}
