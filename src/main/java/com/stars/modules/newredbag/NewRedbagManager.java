package com.stars.modules.newredbag;

import com.stars.modules.newredbag.prodata.FamilyRedbagVo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class NewRedbagManager {

    public final static int REDBAG_TYPE_SELF = 1;   // 自定义红包

    public static int VALID_TIME;   // 红包有效时间
    public static int SELF_COUNT_MAX;   // 自定义红包配置上线
    public static int RECORD_CLEAR_TIME;    // 记录清除时间
    public static int MAX_PADDING;  // 最大增加元宝数

    private static ConcurrentMap<Integer, FamilyRedbagVo> redbagMap = new ConcurrentHashMap<>();

    public static FamilyRedbagVo getFamilyRedbagVo(int redId) {
        return redbagMap.get(redId);
    }

    public static void setRedbagMap(ConcurrentMap<Integer, FamilyRedbagVo> map) {
        redbagMap = map;
    }

}
