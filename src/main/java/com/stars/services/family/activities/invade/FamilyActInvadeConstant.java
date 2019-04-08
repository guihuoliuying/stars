package com.stars.services.family.activities.invade;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class FamilyActInvadeConstant {
    // 怪物类型
    public static byte NORMAL_MONSTER = 1;// 小怪
    public static byte ELITE_MONSTER = 2;// 精英怪
    // npc状态
    public static byte NPC_AVAILABLE = 0;// 未触发
    public static byte NPC_CHALLENGING = 1;// 挑战中
    public static byte NPC_DEAD = 2;// 已死亡
    // 队伍类型
    public static byte TEAM_SINGLE = 0;// 单人
    public static byte TEAM_MULTI = 1;// 多人
    // 排行榜显示前几名
    public static int RANK_SHOW_MAX = 5;
}
