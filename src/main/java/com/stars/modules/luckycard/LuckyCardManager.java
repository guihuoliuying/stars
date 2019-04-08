package com.stars.modules.luckycard;

import com.stars.modules.luckycard.prodata.LuckyCard;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCardManager {
    public static Map<Integer, LuckyCard> luckyCardMap;
    public static int[] basicScale;//每充值x元可获得y张卡券
    public static int[] additionalScale;//每累计充值a元可额外获得b张卡券
    public static Map<Integer, Integer> resolveReward;//一张奖券分解奖励
    public static int luckyCardConsumeUnit;//单次抽卡消耗
    public static List<LuckyCard> normalCards;
    public static List<LuckyCard> specialCards;//稀有卡
    public static List<LuckyCard> allCards;//所有卡
    public static String luckyCardPayPayAward;//充值送奖券
}
