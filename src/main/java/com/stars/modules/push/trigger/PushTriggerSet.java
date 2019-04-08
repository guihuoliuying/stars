package com.stars.modules.push.trigger;

import com.stars.core.event.Event;
import com.stars.core.module.Module;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushTriggerSet {

    private List<PushTrigger> triggerList = new ArrayList<>();

    public PushTriggerSet(int pushId, String triggerSetString) throws Exception {
        parse(pushId, triggerSetString);
    }

    /**
     * triggerSetString := triggerString [| triggerString]...
     * triggerString := triggerName [, triggerArgument]...
     * @param triggerSetString
     * @throws Exception
     */
    public void parse(int pushId, String triggerSetString) throws Exception {
        String[] triggerStringArray = triggerSetString.split("\\|");
        for (String triggerString : triggerStringArray) {
            String[] array = triggerString.split(",");
            Class<? extends PushTrigger> triggerClass = PushTriggerGlobal.getTriggerClass(array[0]);
            if (triggerClass == null) {
                LogUtil.error("推送|不存在触发器:" + array[0]);
                throw new RuntimeException();
            }
            PushTrigger trigger = triggerClass.newInstance();
            trigger.setPushId(pushId);
            if (array.length > 1) {
                String[] args = new String[array.length-1];
                System.arraycopy(array, 1, args, 0, args.length);
                trigger.parse(args);
            }
            triggerList.add(trigger);
        }
    }

    public boolean check(Event event, Map<String, Module> moduleMap) {
        for (PushTrigger trigger : triggerList) {
            if (!trigger.check(event, moduleMap)) {
                return false;
            }
        }
        return true;
    }

    public List<PushTrigger> triggerList() {
        return triggerList;
    }

}
