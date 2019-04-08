package com.stars.services.multicommon;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Map;

/**
 * 主要是用于跨服调用未暴露的游戏服业务！
 * Created by chenkeyu on 2017-08-25.
 */
public interface MultiCommonService extends ActorService, Service {

    @AsyncInvocation
    void sendToSingle(int serverId, long roleId, int templateId, long senderId, String senderName, Map<Integer, Integer> affixMap, String... params); // 异步
}
