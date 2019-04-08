package com.stars.modules.push.conditionparser;

import com.stars.core.module.Module;
import com.stars.modules.push.conditionparser.node.dataset.PushCondDataSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.achievement.PcdsAchievementActivedSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.activedjob.PcdsActivedJobSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.baby.PcdsBabyFashionSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.book.PcdsBookSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.buddy.PcdsBuddySet;
import com.stars.modules.push.conditionparser.node.dataset.impl.chargepref.PcdsChargePrefSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.deity.PcdsDeityWeaponSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.dungeonpassed.PcdsDungeonPassedSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equip.PcdsEquipSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equipoff.PcdsEquipOffSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equipon.PcdsEquipOnSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.equipslot.PcdsEquipSlotSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.fashion.PcdsFashionSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.fashioncard.PcdsFashionCardSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.gem.PcdsGemSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.guest.PcdsGuestSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.guestfeeling.PcdsGuestFeelingSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.offlineHour.PcdsOfflineSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.ride.PcdsRideSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.soul.PcdsSoulGodSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.task.PcdsTaskDoneSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.tool.PcdsToolSet;
import com.stars.modules.push.conditionparser.node.dataset.impl.trump.PcdsTrumpSet;
import com.stars.modules.push.conditionparser.node.func.PushCondFunc;
import com.stars.modules.push.conditionparser.node.func.impl.datetime.PcfNow;
import com.stars.modules.push.conditionparser.node.func.impl.datetime.PcfToHour;
import com.stars.modules.push.conditionparser.node.func.impl.dungeon.PcfDungeonIsActived;
import com.stars.modules.push.conditionparser.node.func.impl.dungeon.PcfDungeonIsPassed;
import com.stars.modules.push.conditionparser.node.func.impl.math.PcfAdd;
import com.stars.modules.push.conditionparser.node.func.impl.math.PcfDiv;
import com.stars.modules.push.conditionparser.node.func.impl.math.PcfMul;
import com.stars.modules.push.conditionparser.node.func.impl.math.PcfSub;
import com.stars.modules.push.conditionparser.node.func.impl.open.PcfIsOpen;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;
import com.stars.modules.push.conditionparser.node.value.impl.*;
import com.stars.modules.push.conditionparser.node.value.impl.fight.*;
import com.stars.modules.push.conditionparser.node.value.impl.loginactivity.PcvLoginActivityComeBack;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.stars.modules.push.conditionparser.PushCondParserTag.*;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PushCondGlobal {

    private static Map<String, PushCondFunc> funcMap = new HashMap<>();
    private static Map<String, PushCondValue> valueMap = new HashMap<>();
    private static Map<String, Class<? extends PushCondDataSet>> dataSetClassMap = new HashMap<>();
    private static Map<String, Set<String>> dataSetFieldMap = new HashMap<>();

    static {
        /* 函数 */
        funcMap.put("now", new PcfNow()); // 获取当前时间
        funcMap.put("tohour", new PcfToHour()); // 将毫秒转换成小时
        funcMap.put("add", new PcfAdd()); // 加
        funcMap.put("sub", new PcfSub()); // 减
        funcMap.put("mul", new PcfMul()); // 乘
        funcMap.put("div", new PcfDiv()); // 除
        funcMap.put("isopen", new PcfIsOpen()); // 判断某个系统是否开启

        funcMap.put("dungeon_isactived", new PcfDungeonIsActived()); // 判断某个关卡是否激活
        funcMap.put("dungeon_ispassed", new PcfDungeonIsPassed()); // 判断某个关卡是否通关

        /* 单值 */
        valueMap.put("level", new PcvLevel()); // 等级
        valueMap.put("vip", new PcvVip()); // vip
        valueMap.put("fight", new PcvFight()); // 战力
        valueMap.put("gold", new PcvGold()); // 元宝
        valueMap.put("money", new PcvMoney()); // 金币
        valueMap.put("vigor", new PcvVigor()); // 体力
        valueMap.put("offlinehours", new PcvOfflineHours()); // 这次登陆和上次登陆的时间差（小时）
        valueMap.put("serverdays", new PcvServerDays()); // 开服的第几天
        valueMap.put("channel", new PcvChannel()); // 渠道号
        valueMap.put("serverid", new PcvServerId()); // 服务id
        valueMap.put("ridelv", new PcvRideLv()); // 坐骑当前等级
        valueMap.put("ridestage", new PcvRideStage()); // 坐骑阶段
        valueMap.put("jobid", new PcvJobId()); // 职业id
        valueMap.put("vipcustomer", new PcvVipCustomer()); // 是否填写过vip信息
        valueMap.put("ismarried", new PcvIsMarried()); // 是否结婚
        valueMap.put("iswxbinded", new PcvIsWxBinded()); // 是否微信绑定
        valueMap.put("charge", new PcvCharge()); // 充值金额
        valueMap.put("chargepref_chosenid", new PcvChargePrefChosenId()); // 充值特惠-选中id
        valueMap.put("discountgift_id", new PcvDiscountGiftId()); // 充值豪礼-giftid
        valueMap.put("discountgift_groupid", new PcvDiscountGiftGroupId()); // 充值豪礼-groupgiftid
        valueMap.put("fight_buddy", new PcvFightBuddy()); // 战力-伙伴
        valueMap.put("fight_familyskill", new PcvFightFamilySkill()); // 战力-伙伴
        valueMap.put("fight_gem", new PcvFightGem()); // 战力-宝石
        valueMap.put("fight_guest", new PcvFightGuest()); // 战力-门客
        valueMap.put("fight_ride", new PcvFightRide()); // 战力-坐骑
        valueMap.put("fight_equip", new PcvFightEquip()); // 战力-装备
        valueMap.put("fight_level", new PcvFightLevel()); // 战力-等级
        valueMap.put("fight_skill", new PcvFightSkill()); // 战力-技能
        valueMap.put("fight_title", new PcvFightTitle()); // 战力-称号
        valueMap.put("loginactivity_comeback", new PcvLoginActivityComeBack()); // 登陆活动-老友归来
        valueMap.put("babystage", new PcvBabyStage()); //宝宝阶段
        valueMap.put("babylv", new PcvBabyLv()); //宝宝等级

        /* 集合 */
        regDataSet("bag", PcdsToolSet.class); // 背包
        regDataSet("equip", PcdsEquipSet.class); // 装备
        regDataSet("equip_on", PcdsEquipOnSet.class); // 装备（身上）
        regDataSet("equip_off", PcdsEquipOffSet.class); // 装备（背包）
        regDataSet("equiphole", PcdsEquipSlotSet.class); // 装备槽
        regDataSet("ride", PcdsRideSet.class); // 坐骑
        regDataSet("buddy", PcdsBuddySet.class); // 伙伴
        regDataSet("deity", PcdsDeityWeaponSet.class); // 神兵
        regDataSet("gem", PcdsGemSet.class); // 宝石
        regDataSet("guest", PcdsGuestSet.class); // 门客
        regDataSet("guestfeel", PcdsGuestFeelingSet.class); // 门客情缘
        regDataSet("book", PcdsBookSet.class); // 典籍
        regDataSet("activedjob", PcdsActivedJobSet.class); // 已激活的职业
        regDataSet("chargepref", PcdsChargePrefSet.class); // 充值特惠集合
        regDataSet("dungeon_passed", PcdsDungeonPassedSet.class); // 通关关卡集合
        regDataSet("task_done", PcdsTaskDoneSet.class); // 完成任务集合
        regDataSet("achievement_actived", PcdsAchievementActivedSet.class); // 激活成就集合
        regDataSet("loginid", PcdsOfflineSet.class); // 账号下各角色数据集合
        regDataSet("trump", PcdsTrumpSet.class); // 法宝
        regDataSet("fashion", PcdsFashionSet.class); //时装
        regDataSet("babyfashion", PcdsBabyFashionSet.class);
        regDataSet("fashioncard", PcdsFashionCardSet.class);
        regDataSet("soulgod", PcdsSoulGodSet.class);//元神
    }

    private static void regDataSet(String name, Class<? extends PushCondDataSet> clazz) {
        try {
            dataSetClassMap.put(name, clazz);
            dataSetFieldMap.put(name, clazz.newInstance().fieldSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PushCondFunc getFunc(String name) {
        return funcMap.get(name);
    }

    public static PushCondValue getValue(String name) {
        return valueMap.get(name);
    }

    public static PushCondDataSet newDataSet(String name, Map<String, Module> moduleMap) {
        Class<? extends PushCondDataSet> clazz = dataSetClassMap.get(name);
        if (clazz == null) {
            LogUtil.error("条件表达式|不存在集合:" + name);
        }
        try {
            return clazz.getConstructor(Map.class).newInstance(moduleMap);
        } catch (Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    public static Set<String> getFieldSet(String name) {
        return dataSetFieldMap.get(name);
    }

    public static String toString(int tag) {
        switch (tag) {
            case TAG_IDENTIFIER:
                return "标识符";
            case TAG_DIGITS:
                return "数值";
            case TAG_STRING:
                return "字符串";
            case TAG_RELATION_OP:
                return "关系运算符";
            case TAG_OR:
                return "或运算";
            case TAG_AND:
                return "与运算";
            case TAG_NOT:
                return "非运算";
            case TAG_IN:
                return "in";
            case TAG_BETWEEN:
                return "between";
            case TAG_EOF:
                return "eof";

            case TAG_PARENTHESIS_LEFT:
                return "(";
            case TAG_PARENTHESIS_RIGHT:
                return ")";
            case TAG_BRACKET_LEFT:
                return "[";
            case TAG_BRACKET_RIGHT:
                return "]";
            case TAG_BRACE_LEFT:
                return "{";
            case TAG_BRACE_RIGHT:
                return "}";
            case TAG_COMMA:
                return ",";
        }
        return "";
    }

}
