package com.stars.modules.chargeback;

import com.stars.modules.chargeback.prodata.BackRule;
import com.stars.modules.chargeback.userdata.AccountChargeBack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/3/20.
 */
public class ChargeBackManager {
    public static final int CHARGE_BACK = 25001;
    public static final int CHARGE_PACKAGE_BACK = 25002;
    // TODO: 2017/3/21 月卡itemid待定
    public static final int MONTHCARD_ITEMID = 2203;

    /**
     * 表示充值返利活动的开启状态
     */
    public static int FLAG = 0;

    /**
     * 充值返还规则
     */
    public static Map<String, BackRule> backRuleMap;
    /**
     * 最低额度返还大礼包字典
     * key:最低额度
     * value：礼包id
     */
    public static Map<Integer, Integer> moneyReward = new LinkedHashMap<>();
    /**
     * <account,AccountChargeBack>
     * 当前活动中用户的奖励领取记录
     */
    public static Map<String, AccountChargeBack> accountChargeBackMap ;

}
