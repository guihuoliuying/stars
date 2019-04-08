package com.stars.services.callboss;

/**
 * Created by liuyuheng on 2016/9/6.
 */
public class CallBossConstant {
    // boss状态
    public static final byte BOSS_STATUS_AVAILABLE = 0;// 可召唤
    public static final byte BOSS_STATUS_ALIVE = 1;// 已召唤,boss存活,可参与击杀
    public static final byte BOSS_STATUS_DEAD  = 2;// 已召唤,boss死亡
    public static final byte BOSS_STATUS_ERROR_TIME = 3;// 未到召唤时间

    // 伤害排行容器最大上限
    public static int RANK_VOLUME_MAX = 999;

    /* 排名(暂定) */
    public static int RANK_INIT = 0;// 初始排名值
    public static int RANK_OVER_1000 = -1;// 999<x
}
