package com.stars.services.offlinepvp;

/**
 * 常量
 * Created by liuyuheng on 2016/10/9.
 */
public class OfflinePvpConstant {
    // 最多尝试匹配多少个等级
    public static int levelMatchLimit = 5;
    // 匹配类型
    public static byte MATCH_TYPE_PLAYER = 1;// 玩家
    public static byte MATCH_TYPE_ROBOT = 2;// 机器人
    // 起服加载常用数据限制
    public static int loadSummaryLimit = 200;
}
