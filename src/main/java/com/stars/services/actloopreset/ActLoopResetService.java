package com.stars.services.actloopreset;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by huwenjun on 2017/11/28.
 */
public interface ActLoopResetService extends Service, ActorService {
    /**
     * 重置活动并修改循环时间
     */
    @AsyncInvocation
    void resetAndLoop();
}
