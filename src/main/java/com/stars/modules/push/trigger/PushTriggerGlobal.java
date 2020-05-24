package com.stars.modules.push.trigger;

import com.stars.modules.push.trigger.impl.PtLogin;
import com.stars.modules.push.trigger.impl.PtRoleLevelUp;
import com.stars.modules.push.trigger.impl.PtUseGold;
import com.stars.modules.push.trigger.impl.PtUseItem;
import com.stars.modules.push.trigger.impl.equip.PtEquipStrengthLevelUp;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushTriggerGlobal {

    private static Map<String, Class<? extends PushTrigger>> triggerClassMap = new HashMap<>();

    static {
        triggerClassMap.put("login", PtLogin.class); // 登录
        triggerClassMap.put("usegold", PtUseGold.class); // 消耗元宝
        triggerClassMap.put("useitem", PtUseItem.class); // 消耗道具
        triggerClassMap.put("rolelevelup", PtRoleLevelUp.class); // 玩家升级
        /* 装备 */
        triggerClassMap.put("equip_strengthup", PtEquipStrengthLevelUp.class); // 装备强化提升
    }

    public static Class<? extends PushTrigger> getTriggerClass(String triggerName) {
        return triggerClassMap.get(triggerName);
    }

}
