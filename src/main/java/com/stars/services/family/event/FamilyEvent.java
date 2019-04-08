package com.stars.services.family.event;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public interface FamilyEvent {

    /* 特殊的 */
    int W_RMB_DONATE = 0x00; // 捐献

    /* 管理 */
    int M_JOIN = 0x10; // 加入
    int M_LEAVE = 0x11; // 离开
    int M_KICKOUT = 0x12; // 强踢
    int M_APPOINT = 0x13; // 任命

    /* 活动*/
    int A_TREASURE = 0x20;//家族探宝事迹

    /* 福利 */
    int W_REDPACKET = 0x30; // 红包

}
