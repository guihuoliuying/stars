package com.stars.modules.luckydraw3;

import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw3Manager {
    public static Map<Integer, LuckyPumpAwardVo> luckyPumpAwardMap;
    /**
     * 配置充值额度及获得的道具，格式：额度|itemid+count
     */
    public static Map<Integer, Map<Integer, Integer>> moneyDrawReward;
    /**
     * 1奖券可转换的道具，格式：itemid+count
     */
    public static Map<Integer, Integer> luckyDrawSwitch;
    /**
     * 配置每日最大抽奖次数
     */
    public static int luckyDrawNumlimit;
    /**
     * 配置每日免费次数，支持填0
     */
    public static int luckyDrawFreeTimes;
    /**
     * 每次抽奖需消耗奖券数量
     */
    public static int luckyDrawConsumeUnit;
    /**
     * 奖品列表
     */
    public static List<LuckyPumpAwardVo> luckyPumpAwardList;
    /**
     * 充值赠送比例
     */
    public static String luckyPumpMoney;
}
