package com.stars.modules.friendInvite;

import com.stars.modules.friendInvite.prodata.InviteVo;

import java.util.Map;

/**
 * Created by chenxie on 2017/6/8.
 */
public class InviteManager {

    public static int INVITEATR_ROLELIMIT;              // 好友邀请和输入邀请码的等级分界
    public static int INVITEATR_REWARD_FIRST;           // 首次奖励
    public static int INVITEATR_REWARD_EVERYTIME;       // 每次奖励
    public static int INVITEATR_REWARD_LIMIT;           // 可领取的邀请奖励上限
    public static int INVITEATR_REWARD_BIND;            // 受邀奖励

    public static Map<String, InviteVo> INVITE_VO_MAP;   // 渠道数据

    public static long SEQUENCE;                        // 自增序列

}
