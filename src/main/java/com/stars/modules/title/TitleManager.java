package com.stars.modules.title;

import com.stars.modules.title.prodata.TitleVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/21.
 */
public class TitleManager {
    /* 常量 */
    // 称号状态
    public static byte TITLE_STATUS_NOTGET = 0;// 未获得
    public static byte TITLE_STATUS_AVAILABLE = 1;// 可使用
    public static byte TITLE_STATUS_OUTDATE = 2;// 已过期
    // 称号使用时间类型
    public static final byte TIME_TYPE_FOREVER = 0;// 永久使用
    public static final byte TIME_TYPE_SPELLTIME = 1;// 一段时间使用
    public static final byte TIME_TYPE_TIMEOVER = 2;// 时间点结束使用

    public static Map<Integer, TitleVo> titleVoMap = new HashMap<>();// 称号产品数据 titleId-vo
    
    public static List<Byte> typeList = new ArrayList<>();//称号类型列表

    public static TitleVo getTitleVo(int titleId) {
        return titleVoMap.get(titleId);
    }
}
