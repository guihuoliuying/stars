package com.stars.modules.serverLog;

import com.stars.util.ServerLogConst;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: huangdimin Date: 16-12-13 Time: 下午6:00 事件类型
 */
public enum EventType {
    USETOOL((short) 1, "使用物品"),
    SELLTOOL((short) 2, "出售物品"),
    BUYTOOL((short) 3, "购买物品"),
    RESOLVETOOL((short) 4, "分解物品"),
    COMPOSETOOL((short) 5, "合成物品"),
    SWEEPDUNGEON((short) 6, "扫荡关卡"),
    CREATFAMILY((short) 7, "创建家族"),
    FAMILYDONATE((short) 8, "家族捐献"),
    FAMILYBUFF((short) 9, "家族心法"),
    FRIENDSEND((short) 10, "好友赠送"),
    EMBEDGEM((short) 11, "宝石镶嵌"),
    LEVELUPGEM((short) 12, "宝石升级"),
    COMPOSEGEM((short) 13, "宝石合成"),
    SUBTOOL((short) 14, "删减物品"),
    MASTERNOTICE((short) 15, "皇榜悬赏"),
    UPMIND((short) 16, "心法升级"),
    UPRIDE((short) 17, "坐骑升级"),
    DUNGEONSCENE((short) 18, "战斗挑战"),
    REVIVE((short) 19, "复活"),
    SHOPBUY((short) 20, "商城购买"),
    SHOPREFRESH((short) 21, "商城刷新"),
    UPSKILL((short) 22, "升级技能"),
    SUBMITTASK((short) 23, "提交任务"),
    GM((short) 24, "GM"),
    TRUMPUP((short) 25, "法宝升级"),
    BUDDYUP((short) 26, "伙伴升阶"),
    PUTONEQUIP((short) 27, "穿上装备"),
    CALLBOSS((short) 28, "召唤boss"),
    DEITYWEAPON((short) 29, "神兵锻造"),
    STRENGTHEQUIP((short) 30, "强化装备"),
    UPSTAREQUIP((short) 31, "装备升星"),
    TRANSFEREQUIP((short) 32, "转移装备"),
    WASHEQUIP((short) 33, "洗练装备"),
    RECOVERWASH((short) 34, "恢复洗练"),
    REFRESHNUM((short) 35, "刷新挑战次数"),
    BUYNUM((short) 36, "购买挑战次数"),
    CREATEADDTOOL((short) 37, "创角添加"),
    AWARD((short) 38, "领奖"),
    DAILYCOUNT((short) 39, "日常次数"),
    DAILYAWARD((short) 40, "日常奖励"),
    GETSART((short) 41, "集星奖励"),
    MAIl((short) 42, "邮件"),
    FAMILYSIZE((short) 43, "家族规模"),
    FAIMLYRED((short) 44, "红包"),
    TINYGAME((short) 45, "小游戏"),
    DISBEDGEM((short) 46, "脱下宝石"),
    ADDTOOL((short) 47, "添加物品"),
    OFFLINEPVP((short) 48, "离线pvp"),
    ONLINEAWARD((short) 49, "在线奖励"),
    FIGHTINGAWADR((short) 50, "战力奖励"),
    BRAVESTAGE((short) 51, "勇者试炼"),
    FAMILYACT((short) 52, "家族活动"),
    SEARCHTRESURE((short) 53, "夺宝"),
    SKYTOWER((short) 54, "天塔"),
    SIGNIN((short) 55, "签到"),
    TEAMDUNGEON((short) 56, "组队挑战"),
    NOTICEMAINSERVER((short) 57, "通知主服"),
    TRUMPRESOLVE((short) 58, "法宝分解"),
    FAMILYBONFIRE((short) 59, "灯笼"),
    FAMILYINVADE((short) 60, "偷袭"),
    PAY((short) 61, "充值"),
    VIP((short) 62, "VIP"),
    RETRIEVEREWARD((short) 63, "奖励找回"),
    ESCORT((short) 64, "押镖"),
    //ACHIEVEMENT((short) 65, "成就"),
    SEVENDAYGOAL((short) 66, "七日目标活动"),
    NEWSERVERSIGN((short) 67, "新服签到活动"),
    AUTHENTIC((short) 68, "鉴宝"),
    GUEST((short) 69, "门客"),
    HOTUPDATE_COMM((short) 70, "comm热更补偿"),
    NEWSERVERFIGHTSCORE((short) 80, "新服冲战力活动奖励"),
    BUT_VIGOR((short) 81, "购买体力"),
    GAMECAVE((short) 82, "新游园"),
    ACTIVE_RIDE((short) 83, "坐骑激活"),
    FAMILY_WAR_REVIVE((short) 84, "家族精英战复活"),
    FAMILY_WAR_SUPPORT((short) 85, "家族战点赞"),
    FAMILY_WAR_PERSONAL_POINT((short) 86, "家族战个人积分奖励"),
    FAMILY_NEW_REDBAG((short) 87, "新版家族红包"),
    FAMILY_ACT_TREASURE((short) 88, "家族探宝"),
    FAMILY_ACT_BONFIRE((short) 89, "家族篝火"),
    FAMILY_BONFIRE_THROW_GOLD((short) 90, "家族篝火-投元宝"),
    FAMILY_BONFIRE_THROW_WOOD((short) 91, "家族篝火-投干柴"),
    ACTIVITY_WORD_EXCHANGE((short) 92, "集字兑换"),
    FINISH_MASTER_NOTICE_BY_GOLD((short) 93, "立刻完成皇榜悬赏"),
    FINISH_BRAVE_BY_GOLD((short) 94, "立刻完成勇者试炼"),
    GM_DEL((short) 95, "gm删除"),
    ACTIVITY_SERVER_FUND((short) 96, "开服基金"),
    SEND_VIGOUR((short) 97, "送体力"),
    FIVE_AWARD((short) 98, "五战奖励"),
    ELITE_DUNGEON((short) 99, "精英副本"),
    FAMILY_TASK_GET_AWARD((short) 100, "家族任务_领取任务完成奖励"),
    FAMILY_TASK_SELF_COMMIT((short) 101, "家族任务_自己提交"),
    FAMILY_TASK_ZERO_RECOVER((short) 102, "家族任务_零点回收"),
    FAMILY_TASK_GUANKA((short) 103, "家族任务关卡"),
    BREAK_GENERAL((short) 104, "普通决裂消耗"),
    BREAK_FORCE((short) 105, "强制决裂消耗"),
    CANDY_REWARD((short) 106, "喜糖奖励"),
    FIREWORKS_COST((short) 107, "烟花消耗"),
    FIREWORKS_AWARD((short) 108, "烟花奖励"),
    REDBAG_SEND_COST((short) 109, "发红包消耗"),
    REDBAG_SENDA_AWARD((short) 110, "发红包奖励"),
    REDBAG_GET((short) 111, "抢红包奖励"),
    PROFRESS_COST((short) 112, "表白消耗"),
    APPOINT_LUXURIOUS_COST((short) 113, "豪华预约消耗"),
    APPOINT_GENERAL_COST((short) 114, "普通预约消耗"),
    APPOINT_AWARD((short) 115, "普通预约奖励"),
    APPOINT_LUXURIOUS_AWARD((short) 116, "豪华预约奖励"),
    APPOINT_RETURN_COST((short) 117, "普通预约拒绝返还"),
    APPOINT_LUXURIOUS_RETURN_COST((short) 118, "豪华预约拒绝返还"),
    FAMILY_ESCORT_LOOT((short) 119, "家族运镖劫镖奖励"),
    RAFFLE_COST((short) 120, "元宝抽奖消耗"),
    RAFFLE_REWARD((short) 121, "元宝抽奖奖励"),
    MARRY_RING_REPLACE((short) 122, "戒指替换"),
    OPEN_BOOK_HOLE((short) 123, "典籍开孔"),
    AWAKE_QUYUAN((short) 124, "唤醒屈原"),
    LEARN_BOOK((short) 125, "领悟典籍"),
    DAILY_5V5_AWARD((short) 126, "日常5v5奖励"),
    SKYRANK_UPGRAD((short) 127, "天梯段位升级"),
    SKYRANK_SEASON_GRAD_AWARD((short) 128, "天梯赛季段位奖励"),
    SKYRANK_SEASON_RANK_AWARD((short) 129, "天梯赛季排名奖励"),
    MARRY_BATTLE((short) 130, "情义副本"),
    SEND_VIGOR_AWARD((short) 131, "赠送体力奖励"),
    ACTIVITE_JOB((short) 132, "激活转职职业"),
    CHANGE_JOB((short) 133, "转职职业"),
    ARCHERY_AWARD((short) 134, "射箭小游戏奖励"),
    TRUE_NAME((short) 135, "实名认证奖励"),
    RENAME((short) 136, "角色改名"),
    ACTIVE_WEAPON((short) 137, "活跃神兵"),
    FRIEND_INVITE((short) 138, "好友邀请"),
    TOKEN_LEVEL_UP_COST((short) 139, "符文升级消耗"),
    TOKEN_WASH_COST((short) 140, "符文装备洗练消耗"),
    TOKEN_MELT((short) 141, "符文溶解"),
    FRIEND_SHARE((short) 142, "朋友圈分享"),
    SKYRANK_DAILY_AWARD((short) 143, "天梯每日奖励"),
    RAFFLE_REWARD_TEN_TIME((short) 144, "十次元宝抽奖奖励"),
    RUNE_DUNGEON((short) 145, "挑战副本"),//符文副本
    CAMP_ENTER_LOW((short) 146, "进入人数少的阵营奖励"),
    CAMP_UPGRADE_OFFICER((short) 147, "升级官职"),
    CAMP_TAKE_DAILY_OFFICER_REWARD((short) 148, "领取每日俸禄"),
    CAMP_DONATE_PROSPEROUS((short) 149, "捐献繁荣度"),
    CAMP_COMPLETE_MISSION((short) 150, "完成任务"),
    RIDE_AWAKE_LEVELUP((short) 151, "坐骑觉醒升级"),
    CAMP_CITY_FIGHT((short) 152, "齐楚之争"),
    CAMP_REPUTATION_RESET((short) 153, "阵营声望重置"),
    GET_DAILY_BALL_SCORE((short) 154, "获得魂值"),
    DAILY_BALL_LEVEL_UP((short) 155, "魂珠升级"),
    DAILY_MUTIPLE_OR_SUPER_AWARD((short) 156, "日常多倍或超级奖励"),
    LUCKY_TURN_TABLE((short) 157, "幸运转盘"),
    OLD_PLAYER_BACK((short) 158, "老玩家回归"),
    CAMP_DONATE_YB((short) 159, "阵营捐献元宝"),
    BABY_SWEEP((short) 160, "宝宝扫荡"),
    BABY_CHANGE_NAME((short) 161, "宝宝改名"),
    BABY_BUY_TIMES((short) 162, "宝宝购买次数"),
    BABY_PRAY_OR_FEED((short) 163, "宝宝求子或培养"),
    BOOK_QUICK_FINISH((short) 163, "典籍快速完成"),
    SEC_SKILL_BUY((short) 164, "限时秒杀购买商品"),
    CAMP_DAILY_SCORE_REWARD((short) 165, "阵营大作战每日积分奖励"),
    CAMP_SINGLE_SCORE_REWARD((short) 166, "阵营大作战单次积分奖励"),
    REFINE((short) 167, "杂物回收"),
    BUY_MARRY_FASHION((short) 168, "购买结婚时装"),
    CHARGE_LUCKY_TICKET((short) 169, "充值赠送幸运抽奖券"),
    LUCKY_DRAW((short) 170, "幸运抽奖"),
    LUCKY_DRAW_END_SWITCH((short) 171, "幸运抽奖活动结束，奖券转换道具"),
    ACHIEVEMENT_STAGE_AWARD((short) 172, "成就段位奖励(提升或完美奖励)"),
    BABY_FASHION_ACTIVE((short) 173, "宝宝时装"),
    JOB_ACTIVE_CARD_REPEAT_RESOLVE((short) 174, "职业激活卡重复分解"),
    DARE_GOD((short) 175, "挑战女神"),
    BABY_FASHION_REPEAT_RESOLVE((short) 176, "宝宝时装相关的"),
    NEW_FIRST_RECHARGE((short) 177, "新首充七日奖励"),
    NEW_FIRST_RECHARGE1((short) 178, "新首充七日奖励(登陆)"),
    MOON_CAKE((short) 179, "接月饼"),
    COLLECT_PHONE((short) 180, "收集号码"),
    TITLE_RESOLVE((short) 181, "称号分解"),
    CHARGE_LUCKY_CARD((short) 182, "幸运卡牌充值赠送抽奖券"),
    LUCKY_CARD((short) 183, "幸运卡牌抽奖"),
    LUCKY_CARD_GET((short) 184, "幸运卡牌取出"),
    LUCKY_CARD_RESOLVE((short) 185, "幸运卡牌分解"),
    LUCKY_CARD_END_RESOLVE((short) 186, "幸运卡牌活动结束卡券分解"),
    FASHION_CARD((short) 187, "时装化身"),
    UPGRADE_EQUIP((short) 188, "装备升级"),
    OPTIONAL_BOX((short) 189, "可选宝箱"),
    SOUL((short) 190, "元神系统");
    private short code;
    private String name;

    public short getCode() {
        return code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    EventType(short code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 检查事件类型是否有重复的
     */
    public static void checkTypeValue() {
        try {
            Map<Short, EventType> map = new Hashtable<Short, EventType>();
            for (EventType e : EventType.values()) {
                if (e.getName().contains("&") || e.getName().contains("=")) {
                    throw new Exception("事件类型名称不能包含=或者&，类型名称：" + e.getCode());
                }
                if (map.containsKey(e.getCode())) {
                    throw new Exception("事件类型值冲突，类型名称：" + e.getCode());
                }
                map.put(e.getCode(), e);
            }
        } catch (Exception e) {
            ServerLogConst.exception.info("checkTypeValue " + e.getMessage(), e);
        }
    }

}
