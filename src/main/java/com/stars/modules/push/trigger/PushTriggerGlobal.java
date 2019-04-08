package com.stars.modules.push.trigger;

import com.stars.modules.push.trigger.impl.*;
import com.stars.modules.push.trigger.impl.buddy.PtBuddyActived;
import com.stars.modules.push.trigger.impl.buddy.PtBuddyArmUp;
import com.stars.modules.push.trigger.impl.buddy.PtBuddyLevelUp;
import com.stars.modules.push.trigger.impl.buddy.PtBuddyStageUp;
import com.stars.modules.push.trigger.impl.deityweapon.PtDeityWeaponGet;
import com.stars.modules.push.trigger.impl.equip.PtEquipStarLevelUp;
import com.stars.modules.push.trigger.impl.equip.PtEquipStrengthLevelUp;
import com.stars.modules.push.trigger.impl.gem.PtGemLevelUp;
import com.stars.modules.push.trigger.impl.ride.PtRideGet;
import com.stars.modules.push.trigger.impl.ride.PtRideLevelUp;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushTriggerGlobal {

    private static Map<String, Class<? extends PushTrigger>> triggerClassMap = new HashMap<>();

    static {
        triggerClassMap.put("login", PtLogin.class); // 登录
        triggerClassMap.put("charge", PtCharge.class); // 充值
        triggerClassMap.put("usegold", PtUseGold.class); // 消耗元宝
        triggerClassMap.put("useitem", PtUseItem.class); // 消耗道具
        triggerClassMap.put("viplevelup", PtVipLevelUp.class); // vip等级提升
        triggerClassMap.put("rolelevelup", PtRoleLevelUp.class); // 玩家升级
        triggerClassMap.put("open", PtOpen.class); // 系统开放
        /* 装备 */
        triggerClassMap.put("equip_starup", PtEquipStarLevelUp.class); // 装备锻造提升
        triggerClassMap.put("equip_strengthup", PtEquipStrengthLevelUp.class); // 装备强化提升
        /* 坐骑 */
        triggerClassMap.put("ride_get", PtRideGet.class); // 坐骑皮肤获得
        triggerClassMap.put("ride_levelup", PtRideLevelUp.class); // 坐骑等级提升
        /* 伙伴 */
        triggerClassMap.put("buddy_get", PtBuddyActived.class); // 伙伴激活
        triggerClassMap.put("buddy_armup", PtBuddyArmUp.class); // 伙伴悟性提升
        triggerClassMap.put("buddy_levelup", PtBuddyLevelUp.class); // 伙伴等级提升
        triggerClassMap.put("buddy_stageup", PtBuddyStageUp.class); // 伙伴阶段提升
        /* 神兵 */
        triggerClassMap.put("deityweapon_get", PtDeityWeaponGet.class); // 神兵获得
        /* 宝石 */
        triggerClassMap.put("gem_levelup", PtGemLevelUp.class); // 宝石升级（合成）
    }

    public static Class<? extends PushTrigger> getTriggerClass(String triggerName) {
        return triggerClassMap.get(triggerName);
    }

}
