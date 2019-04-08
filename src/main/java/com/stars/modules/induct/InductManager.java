package com.stars.modules.induct;

import com.stars.modules.induct.prodata.InductVo;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/18.
 */
public class InductManager {
    /* 常量 */
    // 引导完成
    public static byte INDUCT_STATE_NOT_FINISH = 0;// 未完成
    public static byte INDUCT_STATE_FINISH = 1;// 已完成
    public static byte INDUCT_STATE_FORCE_FINISH = 2;// 强制完成的, 即非玩家手动完成的。（玩家可能触发了这个引导，但是基于引导机制，可能提前将它强制完成了）
    // 引导初始步骤
    public static byte INIT_STEP = 1;
    // 引导循环类型
    public static final byte LOOP_TYPE_ONCE = 1;// 只触发完成一次,可配置执行到某一步就算完成。
    public static final byte LOOP_TYPE_REPEAT = 2;// 可重复触发,重复完成

    public static Map<Integer, InductVo> inductVoMap;// 引导产品数据(初始步骤)

    public static InductVo getInductVo(int inductId) {
        return inductVoMap.get(inductId);
    }
}
