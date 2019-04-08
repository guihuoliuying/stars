package com.stars.services.weeklygift;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public interface WeeklyGiftService extends ActorService, Service {

    @AsyncInvocation
    void doCharge(long roleId, int vipLv, int level, int charge);

    @AsyncInvocation
    void dailyReset();

    @AsyncInvocation
    void initActivity();

    @AsyncInvocation
    void closeActivity();

    @AsyncInvocation
    void initRoleData(long roleId, int vipLv, int level, int charge);

    @AsyncInvocation
    void view(long roleId);

    @AsyncInvocation
    void save();
}
