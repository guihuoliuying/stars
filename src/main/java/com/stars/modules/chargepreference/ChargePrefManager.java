package com.stars.modules.chargepreference;

import com.stars.modules.chargepreference.prodata.ChargePrefVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ChargePrefManager {

    public static int NO_CHOSEN_PREF_ID = -1;

    public static int countLimit = 0;
    public static int rebateEmailTemplateId = 0;
    public static int normalEmailTemplateId = 0;
    public static Map<Integer, ChargePrefVo> prefVoMap;
    public static Map<Integer, ChargePrefVo> pushId2PrefVoMap;

    public static ChargePrefVo getPrefVo(int prefId) {
        return prefVoMap.get(prefId);
    }

    public static ChargePrefVo getPrefVoByPushId(int pushId) {
        return pushId2PrefVoMap.get(pushId);
    }

    public static boolean isNew(int prefId) {
        return prefVoMap.get(prefId).isNew();
    }

}
