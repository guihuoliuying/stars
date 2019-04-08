package com.stars.modules.drop;

import com.stars.modules.drop.prodata.DropVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/30.
 */
public class DropManager {
    public static final byte ITEM_REWARD_TYPE = 0;// 物品奖励类型
    public static final byte DROP_REWARD_TYPE = 1;// 掉落组奖励类型

    public static byte MAXLOOP = 4;// 最大嵌套次数

    // dropId-vo
    public static Map<Integer, DropVo> dropVoMap = new HashMap<>();

    public static Map<Integer,List<DropVo>> dropGroupMap;

    public static DropVo getDropVo(int dropId) {
        return dropVoMap.get(dropId);
    }

    public static List<DropVo> getDropGroup(int dropGroup){
        return dropGroupMap.get(dropGroup);
    }

    /* 生成掉落物品的方法（供跨服使用） */
    public static Map<Integer, Integer> executeDrop(int dropId, int times) {
        return DropUtil.executeDrop(dropId, times);
    }

    public static List<Map<Integer, Integer>> executeDropNotCombine(int dropId, int times) {
        return DropUtil.executeDropNotCombine(dropId, times);
    }

}
