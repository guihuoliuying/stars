package com.stars.modules.authentic;

import com.stars.modules.authentic.prodata.AuthenticVo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/12/22.
 */
public class AuthenticManager {
    public static Map<String, AuthenticVo> authenticVoMap = new HashMap<>();
    public static Map<Integer,Integer> newPlayerMoneyDrop = new HashMap<>();
    public static Map<Integer,Integer> newPlayerGoldDrop = new HashMap<>();

    public static AuthenticVo getAuthenticVo(int level, int type) {
        for (AuthenticVo vo : authenticVoMap.values()) {
            if (level >= vo.getMinLevel() && level <= vo.getMaxLevel() && type == vo.getType()) {
                return vo;
            }
        }
        return null;
    }

}
