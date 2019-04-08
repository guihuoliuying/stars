package com.stars.modules.skytower;

/**
 * 镇妖塔的常量;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerConstant {
    //未完成;
    public static final byte AWARD_NO_COMPLETE = 1;
    //已完成,未领奖;
    public static final byte AWARD_COMPLETE_NO_GET = 2;
    //已领奖;
    public static final byte AWARD_GETTED = 3;

    //一般的请求镇妖塔vo数据;
    public static final byte REQUEST_VO_NORMAL = 1;
    //请求镇妖塔当前层的下一个包含挑战奖励的vo数据;
    public static final byte REQUEST_VO_NEXT_CHALLENGE = 2;

    //请求信息类型;
    public static final byte REQUEST_INFO_TYPE = 1;
    //请求奖励类型-每日;
    public static final byte REQUEST_INFO_GET_AWARD_DAY = 2;
    //请求奖励类型-挑战;
    public static final byte REQUEST_INFO_GET_AWARD_CHALLENGE = 3;
    //请求重置九层塔层数
    public static final byte REQUEST_RESET_LAYER = 4;
}
