package com.stars.modules.familyEscort;

/**
 * Created by zhaowenshuo on 2017/4/22.
 */
public class FamilyEscortConst {

    public static int timeLimitOf1v1Initial = 0; // 1v1的初始阶段（展示玩家信息）
    public static int timeLimitOf1v1Preparation = 15_000; // 1v1的客户端准备阶段（预加载）
    public static int timeLimitOf1v1Countdown = 3_000; // 1v1的客户端倒数
    public static int timeLimitOf1v1 = 60_000; // 1v1战斗时长
    public static int timeoutOf1v1 = timeLimitOf1v1 * 2; // 1v1战斗超时时间

}
