package com.stars.modules.discountgift;

import com.stars.modules.discountgift.prodata.DiscountGiftVo;

import java.util.List;
import java.util.Map;

/**
 * Created by chenxie on 2017/5/26.
 */
public class DiscountGiftManager {

    public static List<DiscountGiftVo> dicGiftVoList;

    public static Map<Integer, List<DiscountGiftVo>> dicGiftVoMap;

    public static int getOriginalPrice(int dropGroupId) {
        if (!dicGiftVoMap.containsKey(dropGroupId)) return 0;
        return dicGiftVoMap.get(dropGroupId).get(0).getOriginalPrice();
    }

}
