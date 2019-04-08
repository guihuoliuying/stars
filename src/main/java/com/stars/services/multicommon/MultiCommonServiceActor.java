package com.stars.services.multicommon;

import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-25.
 */
public class MultiCommonServiceActor extends ServiceActor implements MultiCommonService {
    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.MultiCommonService, this);
    }

    @Override
    public void printState() {

    }

    @Override
    public void sendToSingle(int serverId, long roleId, int templateId, long senderId, String senderName, Map<Integer, Integer> affixMap, String... params) {
        ServiceHelper.emailService().sendToSingle(roleId, templateId, senderId, senderName, affixMap, params);
    }
}
