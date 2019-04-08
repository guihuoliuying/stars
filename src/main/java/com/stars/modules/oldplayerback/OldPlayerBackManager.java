package com.stars.modules.oldplayerback;

import com.stars.modules.oldplayerback.pojo.ActOldPlayerBackDate;
import com.stars.modules.oldplayerback.pojo.AllRoleLimitConf;
import com.stars.modules.oldplayerback.pojo.RewardPosition;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/13.
 */
public class OldPlayerBackManager {
    /**
     * 全角色离线限制条件，单位小时
     */
    public static AllRoleLimitConf ComebackAllRoleLimitConf;
    /**
     * 登陆角色回归限制等级
     */
    public static int ComebackLoginRoleimitLevel;
    /**
     * 老友归来活动时间
     */
    public static ActOldPlayerBackDate ComebackRewardActTime;
    /**
     * 每天奖励映射
     * 《day，RewardPosition》
     */
    public static Map<Integer, RewardPosition> dayReward;
    /**
     * 老玩家回归活动时间
     */
    public static String ComebackRewardActTimeStr;
}
