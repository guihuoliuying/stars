package com.stars.modules.push;

import com.stars.core.annotation.DependOn;
import com.stars.core.db.DBUtil;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.push.listener.PushListener;
import com.stars.modules.push.prodata.PushVo;
import com.stars.modules.push.trigger.PushTrigger;
import com.stars.modules.push.trigger.PushTriggerSet;
import com.stars.network.PlaceholderPacketSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
@DependOn({MConst.Data})
public class PushModuleFactory extends AbstractModuleFactory<PushModule> {

    public PushModuleFactory() {
        super(new PlaceholderPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, PushVo> pushVoMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "pushid", PushVo.class, "select * from push order by `activityid` asc, `group` asc, `grouprank` desc");
        Map<Integer, Map<Integer, Map<Integer, PushVo>>> group2PushVoMap = new HashMap<>();
        Map<Integer, Map<Integer, PushVo>> activityId2PushVoMap = new HashMap<>();
        Map<Class<? extends Event>, List<PushTrigger>> eventClass2TriggerMap = new HashMap<>();

        for (PushVo pushVo : pushVoMap.values()) {
            System.out.println("parse push vo: " + pushVo.getCondition());
            pushVo.init();
            // group
            if (!group2PushVoMap.containsKey(pushVo.getActivityId())) {
                group2PushVoMap.put(pushVo.getActivityId(), new HashMap<Integer, Map<Integer, PushVo>>());
            }
            if (!group2PushVoMap.get(pushVo.getActivityId()).containsKey(pushVo.getGroup())) {
                group2PushVoMap.get(pushVo.getActivityId()).put(pushVo.getGroup(), new HashMap<Integer, PushVo>());
            }
            group2PushVoMap.get(pushVo.getActivityId()).get(pushVo.getGroup()).put(pushVo.getPushId(), pushVo);
            // activity
            if (!activityId2PushVoMap.containsKey(pushVo.getActivityId())) {
                activityId2PushVoMap.put(pushVo.getActivityId(), new HashMap<Integer, PushVo>());
            }
            activityId2PushVoMap.get(pushVo.getActivityId()).put(pushVo.getPushId(), pushVo);
            // event
            PushTriggerSet triggerSet = pushVo.getTriggerSet();
            if (triggerSet != null) {
                for (PushTrigger trigger : triggerSet.triggerList()) {
                    Class<? extends Event> eventClass = trigger.eventClass();
                    if (!eventClass2TriggerMap.containsKey(eventClass)) {
                        eventClass2TriggerMap.put(eventClass, new ArrayList<PushTrigger>());
                    }
                    eventClass2TriggerMap.get(eventClass).add(trigger);
                }
            }
        }

        // 检查同一活动同一组内的触发条件应该是一样的
        for (Map<Integer, Map<Integer, PushVo>> groupSet : group2PushVoMap.values()) {
            for (Map<Integer, PushVo> group : groupSet.values()) {
                PushVo lastVo = null;
                for (PushVo pushVo : group.values()) {
                    if (lastVo != null && !lastVo.getTrigger().trim().equals(pushVo.getTrigger().trim())) {
                        throw new RuntimeException("精准推送|同一活动同一组内的触发条件不一致|" +
                                "pushId:" + lastVo.getPushId() + "|pushId:" + pushVo.getPushId());
                    }
                    lastVo = pushVo;
                }
            }
        }

        PushManager.pushVoMap = pushVoMap;
        PushManager.group2PushVoMap = group2PushVoMap;
        PushManager.activityId2PushVoMap = activityId2PushVoMap;
        PushManager.eventClass2TriggerMap = eventClass2TriggerMap;

    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        PushListener listener = new PushListener((PushModule) module);

        for (Class<? extends Event> eventClass : PushManager.eventClass2TriggerMap.keySet()) {
            eventDispatcher.reg(eventClass, listener);
        }
    }

    @Override
    public PushModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new PushModule(id, self, eventDispatcher, map);
    }
}
