package com.stars.modules.cg;

import com.stars.modules.cg.prodata.CgGroupVo;

import java.util.Map;

/**
 * Created by panzhenfeng on 2017/3/7.
 */
public class CgManager {
    public static byte CG_STATE_NOT_FINISH = 0;// 未完成
    public static byte CG_STATE_FINISH = 1;// 已完成

    public static Map<Integer, CgGroupVo> cgGroupMap;
}
