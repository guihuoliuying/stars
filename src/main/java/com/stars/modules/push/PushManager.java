package com.stars.modules.push;

import com.stars.core.event.Event;
import com.stars.modules.push.prodata.PushVo;
import com.stars.modules.push.trigger.PushTrigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushManager {

    public static final int STATE_INACTIVED = 0x00; // 未激活
    public static final int STATE_ACTIVED = 0x01; // 激活

    public static final int TYPE_ALL = 0x00; // 终身
    public static final int TYPE_DAY = 0x01; // 每天
    public static final int TYPE_WEEK = 0x02; // 每周

    static Map<Integer, PushVo> pushVoMap; // (pushId -> pushVo)
    static Map<Integer, Map<Integer, Map<Integer, PushVo>>> group2PushVoMap; // (activityId -> (groupId -> (pushId -> pushVo)))
    static Map<Integer, Map<Integer, PushVo>> activityId2PushVoMap; // (activityId -> (pushId -> pushVo))
    static Map<Class<? extends Event>, List<PushTrigger>> eventClass2TriggerMap; // (eventClass -> list of trigger)

    public static PushVo getPushVo(int pushId) {
        return pushVoMap.get(pushId);
    }

    public static Map<Integer, PushVo> getPushVoMapByGroup(int activityId, int groupId) {
        Map<Integer, Map<Integer, PushVo>> groupMap = group2PushVoMap.get(activityId);
        if (groupMap != null) {
            Map<Integer, PushVo> pushVoMap = groupMap.get(groupId);
            if (pushVoMap != null) {
                return pushVoMap;
            }
        }
        return new HashMap<>();
    }

    public static Map<Integer, PushVo> getPushVoMapByActivityId(int activityId) {
        return activityId2PushVoMap.get(activityId);
    }

    public static List<PushTrigger> getTriggerList(Class<? extends Event> eventClass) {
        return eventClass2TriggerMap.get(eventClass);
    }

}
