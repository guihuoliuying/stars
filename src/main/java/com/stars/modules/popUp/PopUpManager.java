package com.stars.modules.popUp;

import com.stars.modules.popUp.prodata.PopUpInfo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class PopUpManager {

    public static Map<Integer, PopUpInfo> POP_UP_INFO_MAPS;

    public static Map<Integer, PopUpInfo> getPopUpInfoMaps() {
        return POP_UP_INFO_MAPS;
    }

    public static PopUpInfo getPopUpInfoById(int id){
        if(StringUtil.isEmpty(POP_UP_INFO_MAPS)) return null;
        return POP_UP_INFO_MAPS.get(id);
    }
}
