package com.stars.modules.fashioncard;

import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.prodata.FashionCard;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class FashionCardManager {
    public static Map<Integer, FashionCard> fashionCardMap = new HashMap<>();
    public static Map<Integer, Class<? extends FashionCardEffect>> effectMap = new HashMap<>();

    public static final int ADD_PASSKILL = 1;//增加被动技能
    public static final int ADD_ITEM = 2;//增加道具
    public static final int DEL_SKILL_CD = 3;//减少技能CD
    public static final int OL_ANNOUNCE = 4;//上线公告

    public static final int PUT_ON = 1;//穿上
    public static final int TAKE_OFF = 0;//脱下

    public static void regEffect(int type, Class<? extends FashionCardEffect> clazz) {
        if (effectMap.containsKey(type)) {
            throw new IllegalArgumentException("重复类型注册");
        }
        effectMap.put(type, clazz);
    }

    public static FashionCard getFashionCardById(int id) {
        return fashionCardMap.get(id);
    }

    public static Class<? extends FashionCardEffect> getEffect(int type) {
        return effectMap.get(type);
    }
}
