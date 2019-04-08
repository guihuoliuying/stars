package com.stars.multiserver.familywar.flow;

/**
 * Created by zhaowenshuo on 2016/12/5.
 */
public class FamilyWarState {

    // 总体顺序
    public static int T_IDLE = 0;
    public static int T_LOCAL_KNOCKOUT_PROCESSING = 1;
    public static int T_LOCAL_KNOCKOUT_END = 2;
    public static int T_REMOTE_QUALIFYING_PROCESSING = 3;
    public static int T_REMOTE_QUALIFYING_END = 4;
    public static int T_REMOTE_KNOCKOUT_PROCESSING = 5;
    public static int T_WAITING1 = 6;
    public static int T_WAITING2 = 7;

    // 本服淘汰赛顺序
    public static int LK_IDLE = 0; // 初始
    public static int LK_GENERATE_AGENDA = 0; // 提取家族战力前8名，生成对阵表，锁帮
    public static int LK_GENERATE_TEAM_LIST = 0; // 报名参赛，确定名单
    public static int LK_SUBMIT_TEAM_LIST = 0; // 提交名单（冻结名单）
    public static int LK_QUARTER_PREPARATION = 0; // 八强赛准备（数据准备，倒计时）
    public static int LK_QUARTER_START = 0; // 八强赛开始（创建战斗FightActor）
    public static int LK_QUARTER_PROCESSING = 0; // 八强赛进行中（玩家进入战斗，打，更新个人积分）
    public static int LK_QUARTER_END = 0; // 八强赛结束（结束战斗FightActor，生成四强赛名单）
    public static int LK_SEMI_PREPARATION = 0; // 四强赛准备（数据准备，倒计时）
    public static int LK_SEMI_START = 0; // 四强赛开始（创建战斗FightActor）
    public static int LK_SEMI_PROCESSING = 0; // 四强赛进行中（玩家进入战斗，打，更新个人积分）
    public static int LK_SEMI_END = 0; // 四强赛结束（结束战斗FightActor，生成决赛/34名决赛名单）
    public static int LK_FINAL_PREPARATION = 0; // 决赛、34名决赛准备（数据准备，倒计时）
    public static int LK_FINAL_START = 0; // 决赛、34名决赛开始（创建战斗FightActor）
    public static int LK_FINAL_PROCESSING = 0; // 决赛、34名决赛进行中（玩家进入战斗，打，更新个人积分）
    public static int LK_FINAL_END = 0; // 决赛、34名决赛结束（结束战斗FightActor，发奖，解锁家族）

    // 跨服海选赛顺序

    // 跨服淘汰赛顺序

}
