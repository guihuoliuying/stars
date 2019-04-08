package com.stars.services.localservice;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/12/7.
 */
public interface LocalService extends Service, ActorService {

    @AsyncInvocation
    void sendAward(int serverId, long roleId, short eventType, int emailTemplateId, Map<Integer, Integer> toolMap);

    @AsyncInvocation
    void sendFightingMasterAward(int serverId, Map<Long, String> value);
}
