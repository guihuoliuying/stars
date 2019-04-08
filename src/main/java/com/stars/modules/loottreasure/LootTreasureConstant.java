package com.stars.modules.loottreasure;

/**
 * 夺宝常量;
 * Created by panzhenfeng on 2016/10/11.
 */
public class LootTreasureConstant {

    public final static boolean DEBUG = true;
    public final static int RANK_VOLUME_MAX = 9999;
    public final static int RANK_POLLFROM_SERVER_COUNT = 100;

    /**
     * 注意,这里要加新枚举的话,要从后面开始加,因为客户端解析是使用枚举的顺序做判断的;
     */
    public static enum ACTIVITYSEGMENT{
        ACTIVITYS_READYSTART,
        ACTIVITYS_START,
        PVE_WAIT,
        PVE_START,
        PVP_WAIT,
        PVP_START,
        ACTIVITYSE_END,
    }
}
