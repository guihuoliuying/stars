package com.stars.modules.popUp;

/**
 * Created by wuyuxing on 2017/3/27.
 */
public class PopUpConstant {

    public static final byte FREQUENCY_TYPE_EVERY_TIMES = 1;      //每次都触发
    public static final byte FREQUENCY_TYPE_DAILY_ONE_TIMES = 2;  //一天只触发一次
    public static final byte FREQUENCY_TYPE_WEEKLY_ONE_TIMES = 3;  //每周只触发一次
    
    public static int WEEKLY_RESET_HOUR_NUM = 6;//每周六点重置
    
    public static final int WEEK_TIMES = 7*24*3600;//一周时间   秒
}
