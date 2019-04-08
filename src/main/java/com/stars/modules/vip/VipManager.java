package com.stars.modules.vip;

import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.modules.vip.prodata.VipinfoVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/3.
 */
public class VipManager {
    public static volatile int chargeSwitchState = 0;
    // 月卡每日奖励已领取
    public static byte MONTH_CARD_DAILY_REWARDED = 1;
    // 达到vip等级滚屏公告类型
    public static byte VIP_LEVELUP_NOTICE_TYPE_LOACL = 1;// 本服公告
    public static byte VIP_LEVELUP_NOTICE_TYPE_RM = 2;// 跨服公告

    public static int MONTH_CARD_CHARGEID = 1;// 月卡充值Id

    public static int VIP_REWARD_EMAIL_TEMPLEID = 11901;// vip等级发放奖励邮件模板Id
    public static int VIP_REWARD_EMAIL_TEMPLEID_FIRST = 26005;// 首冲奖励邮件模板Id
    public static int MONTHCARD_AWARD_TEMPLEID = 11902;// 月卡奖励邮件模板Id
    public static String VIP_REWARD_EMAIL_SENDER = "贵族特权";// vip等级发放奖励邮件-发奖人
    // 月卡每日领取奖励
    public static Map<Integer, Integer> monthCardAward = new HashMap<>();
    // 月卡奖励可领取天数
    public static int monthCardDays;
    public static int cardContinueDay;

    // vipinfo,<渠道,<level, vo>>
    public static Map<Integer, VipinfoVo> vipVoMap = new HashMap<>();
    // <渠道,<chargeId, vo>>
    public static Map<String, Map<Integer, ChargeVo>> chargeVoMap = new HashMap<>();

    // <渠道,<iosChargeId, vo>>
    public static Map<String, Map<String, ChargeVo>> chargeVoMap1 = new HashMap<>();

    public static Map<String, List<ChargeVo>> chargetVoList;

    public static Map<Integer, Integer> FINISH_MASTER_NOTICE_COST;   //立刻完成皇榜悬赏消耗
    public static Map<Integer, Integer> FINISH_BRAVE_COST;           //立刻完成勇者试炼消耗
    public static Integer FINISH_BRAVE_DROP_GROUP;                  //立刻完成勇者试炼奖励掉落组
    public static Integer VIP_EXP_COEF;       //VIP经验转换比例

    public static int payCardMoney2GoldRate = 10;    //充值卡剩余的额度转换为元宝的比例

    public static final String VIPCOMPENSATE = "vip.compensate";

    public static VipinfoVo getVipinfoVo(int level) {
        if (vipVoMap == null || level < 0) return null;
        return vipVoMap.get(level);
    }

    public static ChargeVo getChargeVo(String channel, int chargeId) {
        if (!chargeVoMap.containsKey(channel)) {
            return null;
        }
        return chargeVoMap.get(channel).get(chargeId);
    }

    public static ChargeVo getChargeVo(String channel, String iosChargeId) {
        if (!chargeVoMap1.containsKey(channel)) {
            return null;
        }
        return chargeVoMap1.get(channel).get(iosChargeId);
    }
}
