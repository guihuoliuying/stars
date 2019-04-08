package com.stars.modules.family;

import com.stars.modules.family.prodata.FamilySkillVo;
import com.stars.modules.family.submodules.entry.FamilyActEntryFilter;
import com.stars.services.family.main.prodata.FamilyLevelVo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/9/5.
 */
public class FamilyManager {

    public static Map<Integer, FamilyLevelVo> levelVoMap; // 家族等级

    public static Map<String, Integer> skillUpgradeWeightMap = new HashMap<>(); // 家族心法一键升级权重表
    static {
        skillUpgradeWeightMap.put("attack", 7);
        skillUpgradeWeightMap.put("hp", 6);
        skillUpgradeWeightMap.put("defense", 5);
        skillUpgradeWeightMap.put("hit", 4);
        skillUpgradeWeightMap.put("avoid", 3);
        skillUpgradeWeightMap.put("crit", 2);
        skillUpgradeWeightMap.put("anticrit", 1);
    }
    public static Map<String, Map<Integer, FamilySkillVo>> skillVoMap; // 家族心法(属性名, (等级, 产品数据))
    public static int upgradeSkillLevelAmapMaxLoop = 4096;

    public static int nameLenLimit = 6; // 名字长度限制
    public static int noticeLenLimit = 50; // 公告长度限制

    public static int familyRecomListLimit = 10; // 家族推荐列表大小
    public static int creationCost = 1000; // 创建家族的价格（代价）
    public static int applyAllLimitPerTime = 10; // 一键申请（一次申请个数）
    public static int protectionContributionThreshold = 10000; // 防误踢：贡献值阈值（大于此值才能踢）
    public static long protectionTimeLimit = 10000; // 防误踢：离线时间期限（大于此值才能踢）
    public static long autoAbdicationTimeLimit = 7 * 24 * 3600 * 1000; // 自动禅让的期限

    public static int joinEmailTemplateId = 0; // 加入的邮件模板id
    public static int kickOutEmailTemplateId = 0; // 强踢的邮件模板id

    public static int assistantLimit = 0; // 副组长人数限制
    public static int elderLimit = 0; // 长老人数限制

    public static byte donateLimit = 10; // 一天的捐献次数限制
    public static int donateReqItemId = 3; // 捐献的道具id
    public static int donateReqValue = 100000; // 捐献的道具数量
    public static int donateGainedFamilyMoney = 100000; // 捐献获得的家族资金
    public static int donateGainedContribution = 128; // 捐献获得的贡献

    public static byte rmbDonateLimit = 10; // 元宝捐献的次数限制
    public static int rmbDonateReqItemId = 1; // 元宝捐献的道具id
    public static int rmbDonateReqValue = 64; // 元宝捐献的道具数据
    public static int rmbDonateGainedFamilyMoney = 100000; // 元宝捐献获得的家族资金
    public static int rmbDonateGainedContribution = 512; // 元宝捐献获得贡献

    public static double contributionPenaltyRatio = 0.80D; // 离开家族的贡献惩罚

    /* 福利（暂不拆分，产品数据就算要拆分也比较简单） */
    // 红包
    public static int rpCountDivisor = 10; // 每多少人抢一个红包
    public static int rpRmbDonationPerRedPacket = 100; // 每多少元宝能够获得一个红包
    public static Map<Integer, Integer> rpGiverRewardMap = new HashMap<>(); // 发红包的人的回馈列表
    public static int rpSeizerAwardDropId = 0; // 红包的掉落id
    public static int rpTimeout = 180; // 红包超时时间（秒）
    public static int rpRecordMaxSize = 20; // 红包记录列表大小限制

    /* 事迹 */
    public static int donateListLimit = 100; // 捐献记录列表大小限制
    public static int eventListLimit = 100; // 事迹列表大小限制

    public static int emailCount = 10;//族长每日可发的邮件
    public static int maxNumber = 50;//邮件内容限制最大字数

    /* 活动入口 */
    public static ConcurrentMap<Integer, FamilyActEntryFilter> actEntryFilters = new ConcurrentHashMap<>();

}
