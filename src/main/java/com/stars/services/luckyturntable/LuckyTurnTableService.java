package com.stars.services.luckyturntable;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-07-13.
 */
public interface LuckyTurnTableService extends Service, ActorService {

    @AsyncInvocation
    void initActivity();

    @AsyncInvocation
    void closeActivity();

    @AsyncInvocation
    void sendMainIcon(long roleId, boolean open);

    @AsyncInvocation
    void sendLuckyList(long roleId);

    @AsyncInvocation
    void addLuckyList(long roleId, String roleName, int itemId, int count);
}
