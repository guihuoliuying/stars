package com.stars.modules.bestcp520;

import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.modules.bestcp520.prodata.BestCPRankReward;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPManager {
    /**
     * (cpid,BestCP)
     */
    public static Map<Integer, BestCP> bestCPMap;//
    /**
     * (cpid,奖励集合)
     */
    public static Map<Integer, List<BestCPRankReward>> cpRankRewardMap ;
    /**
     * cpid,(ranknum,groupid)
     */
    public static Map<Integer, Map<Integer, Integer>> rankRewardMap ;
    /**
     * (cpid,显示记录条数)
     */
    public static Map<Integer, Integer> bestCPRankDisplayMap ;
    /**
     * 对应每日投票的奖励
     * (votesum，groupid)
     */
    public static Map<Integer, Integer> groupRewardMap ;
    /**
     * 投票物品的itemid
     */
    public static int bestCPTicketItemId;
    /**
     * 活动时间
     */
    public static String timeDesc;
    /**
     * 活动规则
     */
    public static String ruleDesc;
}
