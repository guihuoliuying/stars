package com.stars.modules.deityweapon;

import com.stars.modules.deityweapon.prodata.DeityWeaponLevelVo;
import com.stars.modules.deityweapon.prodata.DeityWeaponVo;

import java.util.Map;

/**
 * 神兵管理器;
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponManager {
    private static Map<Integer, Map<Byte, DeityWeaponVo>> deityWeaponVoMap = null;
    private static Map<Integer, DeityWeaponVo> deityWeaponVoDic = null;
    private static Map<Byte, Map<Integer, DeityWeaponLevelVo>> deityWeaponLevelVoMap = null;
    private static Map<Byte, Integer> initDeityWeaponLevelVoMap = null;
    private static Map<Byte, Integer> maxDeityWeaponLevelVoMap = null;
    public static Map<Integer, DeityWeaponVo> itemDeityWeaponVoMap;

    public static void setGetDeityWeaponVoDic(Map<Integer, DeityWeaponVo> map) {
        deityWeaponVoDic = map;
    }

    public static void setDeityWeaponVoMap(Map<Integer, Map<Byte, DeityWeaponVo>> map) {
        deityWeaponVoMap = map;
    }

    public static void setDeityWeaponLevelVoMap(Map<Byte, Map<Integer, DeityWeaponLevelVo>> map) {
        deityWeaponLevelVoMap = map;
    }

    public static DeityWeaponVo getDeityWeaponVo(int deityWeaponId) {
        return deityWeaponVoDic.get(deityWeaponId);
    }

    public static DeityWeaponVo getDeityWeaponVoByItemId(int itemId) {
        return itemDeityWeaponVoMap.get(itemId);
    }

    public static DeityWeaponVo getDeityWeaponVo(int jobId, byte deityWeaponType) {
        if (deityWeaponVoMap.containsKey(jobId)) {
            if (deityWeaponVoMap.get(jobId).containsKey(deityWeaponType)) {
                return deityWeaponVoMap.get(jobId).get(deityWeaponType);
            }
        }
        return null;
    }


    public static Map<Byte, Map<Integer, DeityWeaponLevelVo>> getDeityWeaponLevelVoMap() {
        return deityWeaponLevelVoMap;
    }

    public static DeityWeaponLevelVo getDeityWeaponLevelVo(byte deityWeaponType, int deityWeaponLevel) {
        if (deityWeaponLevelVoMap.containsKey(deityWeaponType)) {
            if (deityWeaponLevelVoMap.get(deityWeaponType).containsKey(deityWeaponLevel)) {
                return deityWeaponLevelVoMap.get(deityWeaponType).get(deityWeaponLevel);
            }
        }
        return null;
    }

    public static void setInitDeityWeaponLevelVoMap(Map<Byte, Integer> map) {
        initDeityWeaponLevelVoMap = map;
    }

    public static DeityWeaponLevelVo getInitDeityWeaponLevelVo(byte deityWeaponType) {
        int level = initDeityWeaponLevelVoMap.get(deityWeaponType);
        return getDeityWeaponLevelVo(deityWeaponType, level);
    }

    public static void setMaxDeityWeaponLevelVoMap(Map<Byte, Integer> map) {
        maxDeityWeaponLevelVoMap = map;
    }

    public static DeityWeaponLevelVo getMaxDeityWeaponLevelVo(byte deityWeaponType) {
        int level = maxDeityWeaponLevelVoMap.get(deityWeaponType);
        return getDeityWeaponLevelVo(deityWeaponType, level);
    }


}
