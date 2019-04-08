package com.stars.core.actor.invocation;

import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

/**
 * Created by zhaowenshuo on 2016/6/15.
 */
public interface ActorService {

    @AsyncInvocation
    @DispatchAll
    void printState();

}
