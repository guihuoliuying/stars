package com.stars.services.opactsceondkill;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

/**
 * Created by chenkeyu on 2017-09-20.
 */
public interface OpActSecondKillService extends Service, ActorService {

    @AsyncInvocation
    void updateSceondKillState(boolean isOpen, int nextStep);
}
