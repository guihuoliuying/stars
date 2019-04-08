package com.stars.modules.refine;

import com.stars.modules.refine.prodata.RefineVo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RefineManager {
    public static boolean isOpen = true;
    public static Map<Integer, RefineVo> refineVoMap = new HashMap<>();

    public static String getOutput(int itemId) {
        if (refineVoMap.containsKey(itemId)) {
            return refineVoMap.get(itemId).getOutput();
        } else {
            return "";
        }
    }

    public static int getOrder(int itemId) {
        if (refineVoMap.containsKey(itemId)) {
            return refineVoMap.get(itemId).getOrder();
        } else {
            return -1;
        }
    }
}
