package com.stars.services.family.activities.entry;

import com.stars.services.Service;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public interface FamilyActEntryService extends Service {

    @AsyncInvocation
    void setOptions(int activityId, int flag, int countdown, String text);

    @AsyncInvocation
    void sendEntryList(long roleId, Map<Integer, Integer> maskMap, List<Integer> notShowList);

}
