package com.stars.modules.foreshow;

import com.stars.modules.foreshow.prodata.ForeShowVo;
import com.stars.modules.foreshow.prodata.ShowSystemVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowManager {

    public static boolean loginCheck = true;//默认登陆检查

    public static Map<String, ShowSystemVo> showSystemVoMap = new HashMap<>();
    public static Map<String, ForeShowVo> foreShowVoMap = new HashMap<>();
    public static Map<Integer, ForeShowVo> foreShowSerialMap = new HashMap<>();

    public static Map<String, ShowSystemVo> getShowSystemVoMap() {
        return showSystemVoMap;
    }

    public static void setShowSystemVoMap(Map<String, ShowSystemVo> showSystemVoMap) {
        ForeShowManager.showSystemVoMap = showSystemVoMap;
    }

    public static List<Integer> getIdList(String name) {
        if (showSystemVoMap.containsKey(name)) {
            return showSystemVoMap.get(name).getIdList();
        } else {
            return new ArrayList<>();
        }
    }

    public static boolean containSysName(String name) {
        return showSystemVoMap.containsKey(name);
    }

    public static ForeShowVo getForeShowVoMap(String name) {
        return ForeShowManager.foreShowVoMap.get(name);
    }

    public static void setForeShowVoMap(Map<String, ForeShowVo> foreShowVoMap) {
        ForeShowManager.foreShowVoMap = foreShowVoMap;
    }

    public static Map<Integer, ForeShowVo> getForeShowSerialMap() {
        return foreShowSerialMap;
    }

    public static void setForeShowSerialMap(Map<Integer, ForeShowVo> foreShowSerialMap) {
        ForeShowManager.foreShowSerialMap = foreShowSerialMap;
    }
}
