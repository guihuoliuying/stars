package com.stars.modules.serverLog;

public enum ThemeType {
    REGEDIT("gamesvr_reg", 0, "gamesvr_reg"),//核心日志-账号相关
    LOGIN("gamesvr_act", 1, "gamesvr_login"),//核心日志-账号相关
    LOGOUT("gamesvr_act", 2, "gamesvr_logout"),//核心日志-账号相关
    ROLEREG("role_reg", 3, "role_reg"),//核心日志-角色相关
    ROLELOGIN("role_act", 4, "role_login"),
    ROLELOGOUT("role_act", 5, "role_logout"),
    STAGE1_ACCOUNT("stat_account", 6, "账号统计"),
    STAGE1_GOLD("stat_main_coin", 7, "库存充值币统计"),
    STAGE1_BANDGOLD("stat_gift_coin", 9, "库存绑定值币统计"),
    STAGE1_MONEY("stat_sub_coin", 10, "库存银两统计"),
    STAGE1_ONLINE("stat_role_online", 11, "在线人数"),
    DUNGEON_WIN("case", 12, "case_win"),//关卡通关
    DUNGEON_START("case", 13, "case_start"),//关卡开始
    DUNGEON_FAIL("case", 14, "case_fail"),//关卡结束

    ACTIVITY_START("activity", 15, "activity_start"),
    ACTIVITY_WIN("activity", 16, "activity_win"),
    ACTIVITY_FAIL("activity", 17, "activity_fail"),

    TASK_START("task", 18, "task_start"),
    TASK_WIN("task", 19, "task_win"),
    TASK_FALI("task", 20, "task_fail"),

    COIN_GOLD("core_coin", 21, "main_coin"),
    COIN_BANDGOLD("core_coin", 22, "gift_coin"),
    COIN_MONEY("core_coin", 23, "sub_coin"),

    STAGE2_LEVEL("core_stat_2", 24, "stat_role_level"),
    STAGE2_VIP("core_stat_2", 25, "stat_account_vip"),

    FINANCE_ADD("core_finance", 26, "finance_main_coin_add"),
    FINANCE_SUB("core_finance", 27, "finance_main_coin_consume"),
    FINANCE_HOLD("core_finance", 28, "finance_main_coin_hold"),

    FRIEND_APPLY("friend_apply", 411, "好友申请"),
    FRIEND_ACCEPT("friend_accept", 411, "接受好友"),
    FRIEND_PHYSICAL("friend_physical", 411, "送体力"),
    FRIEND_FLOWER("friend_flower", 411, "送花"),
    FRIEND_BLACKLIST("friend_blacklist", 411, "拉黑名单"),
    FRIEND_FIGHT("friend_fight", 411, "好友切磋"),

    /*******运营静态日志******/
    STATIC_EQUIP("static_equip", 405, "装备静态日志"),
    STATIC_RIDE("static_ride", 406, "坐骑静态日志"),
    STATIC_DEITY("static_deity", 407, "神兵静态日志"),
    STATIC_GEM("static_gem", 408, "宝石静态日志"),
    STATIC_GUEST("static_guest", 409, "门客静态日志"),
    STATIC_SKILL("static_skill", 410, "技能静态日志"),
    STATIC_FAMILY("static_family", 412, "家族静态日志"),
    STATIC_FIGHTING("static_fighting", 414, "战力构成静态日志"),
    STATIC_ONLINE_AWARD("static_online_award", 415, "在线奖励静态日志"),
    STATIC_SEVEN_DAY_GOAL("static_seven_day_goal", 416, "七日目标静态日志"),
    STATIC_NEW_SERVER_SIGN("static_new_server_sign", 417, "新服签到静态日志"),
    STATIC_TITLE("static_new_server_sign", 438, "称号静态日志"),
    BUDDY_LOG("static_4", 439, "伙伴日志"),
    STATIC_BOOK_LOG("static_4", 449, "典籍"),
    STATIC_FASHION_LOG("static_4", 457, "时装"),
    STATIC_SKYRANK("static_4", 468, "天梯段位"),
    STATIC_TOKEN_EQUIPMENT("static_4", 486, "符文装备"),

    /************************************************/

    /*******运营动态日志******/
    DYNAMIC_NEW_SERVER_SIGN("dynamic_new_server_sign", 430, "新服签到动态日志"),
    DYNAMIC_SHOP_BUY("dynamic_4", 440, "商店购买"),
    DYNAMIC_FRIEND("dynamic_4", 411, "好友日志"),
    DYNAMIC_FAMILY("dynamic_4", 413, "家族个人日志"),
    DYNAMIC_MARRY("dynamic_4", 447, "结婚日志"),
    DYNAMIC_DUNGEON_RECONNECT("dynamic_4", 472, "关卡重连次数"),
    FAMILY_WAR("dynamic_4", 456, "家族战日志"),
    DYNAMIC_DAILY_5V5("dynamic_4", 467, "日常5v5"),
    DYNAMIC_FRIEND_INVATE("dynamic_4", 478, "好友邀请"),
    DYNAMIC_WX_SHARED("dynamic_4", 479, "微信朋友圈分享"),
    DYNAMIC_WEEKLYGIFT("dynamic_4", 488, "周惠礼包"),
    DYNAMIC_DAIYLY("dynamic_4", 500, "升战力"),
    DYNAMIC_SECSKILL("dynamic_4", 506, "限时秒杀"),
    DYNAMIC_BABY("dynamic_4", 507, "宝宝"),
    DYNAMIC_REFINE("dynamic_4", 508, "杂物回收"),

    /************************************************/

    /**
     * 特殊账号告警日志
     */
    SPECIAL_ACCOUNT("dynamic_special_account", 431, "特殊账号告警日志"),


    /*活动类型code 9开头**/
    ACTIVITY_1("activity", 901, "登陆游戏"),
    ACTIVITY_10("activity", 910, "召唤领主"),
    ACTIVITY_11("activity", 911, "九层高塔"),
    ACTIVITY_12("activity", 912, "守护屈原"),
    ACTIVITY_13("activity", 913, "组队挑战"),
    ACTIVITY_18("activity", 918, "经验产出副本"),
    ACTIVITY_2("activity", 902, "扫荡关卡"),
    ACTIVITY_20("activity", 920, "勇者试炼"),
    ACTIVITY_21("activity", 921, "王榜悬赏"),
    ACTIVITY_22("activity", 922, "强化石产出副本"),
    ACTIVITY_23("activity", 923, "签到"),
    ACTIVITY_24("activity", 924, "运送物资"),
    ACTIVITY_25("activity", 925, "坐骑副本"),
    ACTIVITY_8("activity", 908, "游园 "),
    ACTIVITY_19("activity", 909, "六国寻宝"),
    ACTIVITY_27("activity", 927, "家族探宝"),
    ACTIVITY_28("activity", 928, "家族探宝周日关卡"),
    ACTIVITY_30("activity", 930, "离线竞技场"),
    ACTIVITY_33("activity", 933, "情义副本"),
    ACTIVITY_39("activity", 939, "节日副本"),
    ACTIVITY_32("activity", 932, "精英副本"),
    ACTIVITY_41("activity", 941, "家族运镖"),
    ACTIVITY_42("activity", 942, "挑战副本"),
    ACTIVITY_43("activity", 943, "守护官职"),
    ACTIVITY_44("activity", 944, "秦楚之争"),
    ACTIVITY_45("activity", 945, "秦楚大作战"),
    ACTIVITY_46("activity", 946, "挑战女神"),

    /*非配置表活动*/
    ACTIVITY_101("activity", 9101, "野外夺宝"),
    ACTIVITY_102("activity", 9102, "演武场"),
    ACTIVITY_103("activity", 9103, "巅峰对决"),
    ACTIVITY_104("activity", 9104, "日常5v5"),;

    private String operateId;
    private int themeId;
    private String operateName;

    ThemeType(String operateId, int themeId, String operateName) {
        this.operateId = operateId;
        this.themeId = themeId;
        this.operateName = operateName;
    }

    public String getOperateId() {
        return operateId;
    }

    public void setOperateId(String operateId) {
        this.operateId = operateId;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public String getOperateName() {
        return operateName;
    }
}
