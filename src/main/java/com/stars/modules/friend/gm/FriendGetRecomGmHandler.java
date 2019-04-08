package com.stars.modules.friend.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class FriendGetRecomGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        ServiceHelper.friendService().sendRecommendationList(roleId, 0);
    }

}
