package com.stars.modules.activeweapon;

import com.stars.modules.activeweapon.prodata.ActiveWeaponVo;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ActiveWeaponManager {
    public static Map<Integer, ActiveWeaponVo> activeWeaponVoMap;//activeWeapon产品数据
    public static Map<Integer, Map<Integer, Integer>> showItemMap;//字符串，id(activeweapon表里的条件ID)+itemid|id+itmeid，表示活动界面需要显示的奖励物品，与条件上显示对应
    public static String activeweaponItemshow;//奖励展示
}
