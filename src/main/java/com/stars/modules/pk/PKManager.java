package com.stars.modules.pk;

/**
 * Created by liuyuheng on 2016/11/1.
 */
public class PKManager {
    public static int recordMax;// 记录最大条数
    public static int pkStageId;// pk使用战斗场景id
    public static long inviteAvailableTime;// 邀请有效时长(ms)
    public static long pvpLimitTime;// pvp限制时长(ms) = stageinfo配置失败时间 + 准备时长
}
