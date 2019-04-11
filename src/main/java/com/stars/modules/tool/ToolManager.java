package com.stars.modules.tool;

import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Garwah on 2016/2/23.
 */
public class ToolManager {

    public static int EMAIL_ID = 18001; // 背包满了，要发送的邮件ID
    public static int EQUIP_FULL_EMAIL_ID = 19101; // 背包满了，要发送的邮件ID

    /**
     * 背包类型
     */
    public static byte ERR_BAG = -1;//增加物品发生异常时的返回

    public static byte RESOUCE_BAG = 0;//资源
    public static byte ITEM_BAG = 1;//材料背包
    public static byte EQUIP_BAG = 2;//装备背包
    public static byte FAMILY_CONTRIBUTION_BAG = 8; // 家族

    public static byte FLUSH_BAG_TYPE_ALL = 0;      //刷新背包类型-全部背包
    public static byte FLUSH_BAG_TYPE_ITEM = 1;     //刷新背包类型-道具背包
    public static byte FLUSH_BAG_TYPE_EQUIP = 2;    //刷新背包类型-装备背包

    /**
     * 最大格子数上限
     * 没接入数据,先这样写
     */
    public static int ITEM_MAX_GRID = 50;
    public static int EQUIP_MAX_GRID = 50;


    /**
     * 资源的类型，对应item表的id
     */
    public final static int GOLD = 1;//金币
    public final static int BANDGOLD = 2;//绑定金币
    public final static int MONEY = 3;//银两
    public final static int VIGOR = 4;//体力
    public final static int EXP = 5;//经验
    public final static int DAILY = 6;//活跃度
    public final static int TRUMPEXP = 7;   // 法宝经验
    public final static int GLORYPOINTS = 8;// 荣誉点
    public final static int SKILLPOINTS = 9;//技能点
    public final static int EXP_BOX = 15;//经验等级系数宝箱
    public final static int VIP_EXP = 16;//vip经验
    public final static int DAILY_BALL_SCORE = 18; //斗魂值
    public final static int BABY_ENERGY = 19;//宝宝精力
    public final static int FAMILY_CONTRIBUTION = 51; // 家族贡献
    public final static int FAMILY_MONEY = 54; // 家族资金
    public final static int VIRTUAL_MONEY = 58;//虚拟币
    public final static int FEATS = 59;//功勋
    public final static int REPUTATION = 60;//声望
    public final static int PROSPEROUS = 61;//繁荣度
    public final static int LUCKY_DRAW_TICKET = 62;//抽奖券
    public final static int LUCKY_CARD_TICKET = 63;//幸运卡牌抽奖券
    public static Set<Integer> resourceIdSet = new HashSet<>();// 资源IdSet

    /**
     * 道具类型
     */
    public static byte TYPE_MONEY = 1;//货币
    public static byte TYPE_MATERIAL = 2;//材料
    public static byte TYPE_DIAMOND = 3;//宝石
    public static byte TYPE_BOX = 4;//宝箱
    public static byte TYPE_OTHER = 5;//其它
    public static byte TYPE_SEARCHTREASURE = 6;//探宝地图专用道具;
    public static byte TYPE_EQUIPMENT = 7;//装备;
    public static byte TYPE_FAMILY_TASK = 8;//家族任务道具;


    public static byte FUNC_TYPE_EXP = 1;//经验类道具
    public static byte FUNC_TYPE_BOX = 2;//宝箱类道具
    public static byte FUNC_TYPE_UI = 3;//特定UI使用的道具
    public static byte FUNC_TYPE_DROP = 4;//掉落类型的宝箱
    public static byte FUNC_TYPE_TITLE = 5;// 称号类道具
    public static byte FUNC_TYPE_UNLOCKEQUIP = 6;//解锁装备
    public static byte FUNC_TYPE_BUDDYEXP = 7;// 伙伴经验
    public static byte FUNC_TYPE_BUDDYEXPBOX = 8;// 增加伙伴经验道具
    public static byte FUNC_TYPE_HP = 9; //血量道具;
    public static byte FUNC_TYPE_KILLMONSTER = 10; //击杀怪物道具;
    public static byte FUNC_TYPE_CLEAR_CD = 11; //清除CD
    public static byte FUNC_TYPE_ADD_BUFF = 12; //添加buff
    public static byte FUNC_TYPE_TRUMP = 13;    // 法宝道具
    public static byte FUNC_TYPE_RIDE = 14;    // 坐骑道具
    public static byte FUNC_TYPE_FASHION = 15;    // 时装道具
    public static byte FUNC_TYPE_JOB_BOX = 16;    //职业宝箱->按职业发放道具
    public static byte FUNC_TYPE_FRIEND_FLOWER = 17;    // 鲜花道具
    public static byte FUNC_TYPE_BUDDY_EQUIP = 18;// 伙伴武装道具
    public static byte FUNC_TYPE_ACTIVE_DEITYWEAPON = 19; //激活神兵;
    public static byte FUNC_TYPE_LEVEL = 20;//等级系数功能
    public static byte FUNC_TYPE_GUEST_MISSION = 21;    // 门客任务道具
    public static byte FUNC_TYPE_FAMILY_REDBAG = 22;    // 家族红包道具
    public static byte FUNC_TYPE_BOX_NO_TIPS = 23;    // 宝箱类道具-无提示
    public static byte FUNC_TYPE_VIP_EXP = 24;    // vip经验
    public static byte FUNC_TYPE_MONTHCARD_DAYS = 25;    // 月卡
    public static byte FUNC_TYPE_MARRY_RING = 26;   // 婚戒
    public static byte FUNC_TYPE_DRAGON_BOAT = 27;   // 龙舟投票
    public static byte FUNC_TYPE_BOOK = 28;   // 典籍碎片
    public static byte FUNC_TYPE_ACTIVE_JOB = 29;   // 解锁卡
    public static byte FUNC_TYPE_MERGE_SERVER_VIP_UPDATED = 30; // 合区补偿道具: 触发重新计算vip等级
    public static byte FUNC_TYPE_BABY_FASHION = 31; // 宝宝时装
    public static byte FUNC_TYPE_FAHION_CARD = 32;//时装化身卡
    public static byte FUNC_TYPE_OPTIONALTOOL = 33;//自选礼包
    public static byte FUNC_FAKE_PAYMENT = 99; //假充值道具


    // 出生增加背包道具
    public static Map<Integer, Integer> birthAddItemMap = new HashMap<>();

    /**
     * 道具类型和道具类型的映射表
     * toolType -> toolFunc
     */
    public static HashMap<Byte, Class<? extends ToolFunc>> funcMap = new HashMap<>();
    /**
     * <kaid,<itemid,num>>
     */
    public static Map<Integer, Map<Integer, Integer>> UC_GIFT_MAP;
    /**
     * <kaid,emailtemplate>
     */
    public static Map<Integer, Integer> UC_GIFT_EMAIL_MAP;

    static {
        resourceIdSet.add(GOLD);
        resourceIdSet.add(MONEY);
        resourceIdSet.add(BANDGOLD);
        resourceIdSet.add(VIGOR);
        resourceIdSet.add(EXP);
        resourceIdSet.add(DAILY);
        resourceIdSet.add(TRUMPEXP);
        resourceIdSet.add(GLORYPOINTS);
        resourceIdSet.add(SKILLPOINTS);
        resourceIdSet.add(VIRTUAL_MONEY);
        resourceIdSet.add(FEATS);
        resourceIdSet.add(REPUTATION);
        resourceIdSet.add(PROSPEROUS);
        resourceIdSet.add(DAILY_BALL_SCORE);
        resourceIdSet.add(BABY_ENERGY);
        resourceIdSet.add(LUCKY_DRAW_TICKET);
        resourceIdSet.add(LUCKY_CARD_TICKET);
    }

    /**
     * 根据物品id决定物品放在哪个背包
     * id为
     */
    public static byte getBagType(int itemId) {
        if (itemId <= 0) { //异常
            return ERR_BAG;
        }
        if (isResource(itemId)) {
            return RESOUCE_BAG;
        }
        if (isFamilyContribution(itemId)) {
            return FAMILY_CONTRIBUTION_BAG;
        }
        if (isEquip(itemId)) {
            return EQUIP_BAG;
        }
        if (isTool(itemId)) {
            return ITEM_BAG;
        }

        return ERR_BAG;
    }

    public static boolean isEquip(int itemId) {
        return false;
    }

    public static boolean isResource(int itemId) {
        return resourceIdSet.contains(itemId);
    }

    public static boolean isFamilyContribution(int itemId) {
        return itemId == FAMILY_CONTRIBUTION;
    }

    /**
     * 产品数据列表
     */
    public static Map<Integer, ItemVo> TOOL_MAP = new HashMap<>();

    public static Map<Integer, ItemVo> TOOL_MAP_COE = new HashMap<>();

    public static ItemVo getItemVo(int itemId) {
        return TOOL_MAP.get(itemId);
    }

    /**
     * 判断一个道具的id是否是道具
     * 资源类也属于道具,但是不会放在背包,直接给人物模块加上
     */
    public static boolean isTool(int itemId) {
        return TOOL_MAP.containsKey(itemId);
    }


    public static void regToolFunc(byte type, Class<? extends ToolFunc> clazz) {
        if (funcMap.containsKey(type)) {
            throw new IllegalArgumentException("重复类型注册");
        }
        funcMap.put(type, clazz);
    }

    public static Class<? extends ToolFunc> getToolFunc(byte type) {
        return funcMap.get(type);
    }

    public static String getFirstItemName(Map<Integer, Integer> map) {
        for (Integer itemId : map.keySet()) {
            ItemVo itemVo = getItemVo(itemId);
            if (itemVo != null) {
                return itemVo.getName();
            }
        }
        return "";
    }

    public static String getItemName(int itemId) {
        ItemVo itemVo = getItemVo(itemId);
        if (itemVo != null) {
            return itemVo.getName();
        }
        return "";
    }

    /**
     * 字符串 -> item map  分割用,
     */
    public static Map<Integer, Integer> parseString(String toolStr) {
        return StringUtil.toMap(toolStr, Integer.class, Integer.class, '+', ',');
    }

    /**
     * 字符串 -> item map 分割用|
     */
    public static Map<Integer, Integer> parseString2(String toolStr) {
        return StringUtil.toMap(toolStr, Integer.class, Integer.class, '+', '|');
    }
}
