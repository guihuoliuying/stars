package com.stars.modules.push.event;

import com.stars.core.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * fixme: 应该批量抛出激活的pushId
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PushActivedEvent extends Event {

    private Map<Integer, PushInfo> pushInfoMap;

    public PushActivedEvent() {
        this.pushInfoMap = new HashMap<>();
    }

    public PushActivedEvent(Map<Integer, PushInfo> pushInfoMap) {
        this.pushInfoMap = pushInfoMap;
    }

    public void addPushInfo(int activityId, int group, int pushId) {
        this.pushInfoMap.put(pushId, new PushInfo(activityId, group, pushId));
    }

    public Map<Integer, PushInfo> getPushInfoMap() {
        return pushInfoMap;
    }
    
}
