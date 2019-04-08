package com.stars.services.family.event;

import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public interface FamilyEventService extends Service, ActorService {

    @AsyncInvocation
    void online(long familyId);

    @AsyncInvocation
    void offline(long familyId);

    @AsyncInvocation
    void save();

    @AsyncInvocation
    void logEvent(long familyId, int event, String... params);

    @AsyncInvocation
    void sendEvent(FamilyAuth auth, byte subtype);

}
