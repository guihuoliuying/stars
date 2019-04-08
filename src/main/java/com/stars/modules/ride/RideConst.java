package com.stars.modules.ride;

/**
 * Created by zhaowenshuo on 2016/9/18.
 */
public class RideConst {

    public static final byte NOT_ACTIVE = 0; // 非激活状态
    public static final byte ACTIVE = 1; // 激活状态（骑乘）

    public static final byte NOT_OWNED = 0; // 还没拥有
    public static final byte OWNED = 1; // 已拥有
    
    public static final byte FIRST_GET = 1; // 已获得过


    public static final int NO_RIDE_ID = -1; // 用于标识没有骑乘坐骑

    public static final int DEFAULT_LEVEL = 1; // 默认等级

    public static final byte NOT_CLICK = 0;//新坐骑
    public static final byte CLICK = 1;//旧坐骑

    public static final int DEFAULT_AWAKE_LEVEL = 1; // 默认的觉醒等级
    
    public static byte REQ_TYPE_TOOL_ACTIVE = 2;//物品激活
    public static byte REQ_TYPE_BUY_ACTIVE = 3;//购买激活
    public static byte REQ_TYPE_AUTO_ACTIVE = 4;//自动激活
    
    public static final byte TIME_LIMIT_TYPE0 = 0;
    public static final byte TIME_LIMIT_TYPE1 = 1;
    public static final byte TIME_LIMIT_TYPE2 = 2;
    
}
