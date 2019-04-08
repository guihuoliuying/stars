package com.stars.modules.fashion;

import com.stars.modules.fashion.prodata.FashionAttrVo;
import com.stars.modules.fashion.prodata.FashionVo;

import java.util.Map;


/**
 * 时装管理器;
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionManager {
    private static Map<Integer, FashionVo> fashionVoMap = null;
    private static Map<Integer, FashionAttrVo> fashionAttrVoMap = null;

    private static int marryFashionItemId; //结婚礼服礼包的code
    private static int marryFashionBuyCount; //一次购买的数量
    private static int buyMarryFashionItemId; //购买结婚礼服使用的道具（货币）
    private static int buyMarryfashionReqCount; //购买结婚礼服需要的道具数量
    /**
     * 职业类型组装
     * 《jobid，《type，fashionvo》
     */
    public static Map<Integer, Map<Integer, FashionVo>> jobFashionMap;

    public static void setFashionAttrVoMap(Map<Integer, FashionAttrVo> fashionAttrVoMap) {
        FashionManager.fashionAttrVoMap = fashionAttrVoMap;
    }

    public static Map<Integer, FashionAttrVo> getFashionAttrVoMap() {
        return fashionAttrVoMap;
    }

    public static void setFashionVoMap(Map<Integer, FashionVo> map) {
        fashionVoMap = map;
    }

    public static Map<Integer, FashionVo> getFashionVoMap() {
        return fashionVoMap;
    }

    public static FashionVo getFashionVo(int fashionId) {
        if (fashionVoMap.containsKey(fashionId)) {
            return fashionVoMap.get(fashionId);
        }
        return null;
    }

    public static FashionAttrVo getFasionAttrVo(int fashionId) {
        if (fashionAttrVoMap.containsKey(fashionId)) {
            return fashionAttrVoMap.get(fashionId);
        }
        return null;
    }

    public static boolean isTimeLimitedFashion(int fashionId) {
        if (fashionAttrVoMap.containsKey(fashionId)) {
            return fashionAttrVoMap.get(fashionId).getTimeType() == (byte) 1;
        }
        return false;
    }

    public static int getMarryFashionItemId() {
        return marryFashionItemId;
    }

    public static void setMarryFashionItemId(int marryFashionItemId) {
        FashionManager.marryFashionItemId = marryFashionItemId;
    }

    public static int getMarryFashionBuyCount() {
        return marryFashionBuyCount;
    }

    public static void setMarryFashionBuyCount(int marryFashionBuyCount) {
        FashionManager.marryFashionBuyCount = marryFashionBuyCount;
    }

    public static int getBuyMarryFashionItemId() {
        return buyMarryFashionItemId;
    }

    public static void setBuyMarryFashionItemId(int buyMarryFashionItemId) {
        FashionManager.buyMarryFashionItemId = buyMarryFashionItemId;
    }

    public static int getBuyMarryfashionReqCount() {
        return buyMarryfashionReqCount;
    }

    public static void setBuyMarryfashionReqCount(int buyMarryfashionReqCount) {
        FashionManager.buyMarryfashionReqCount = buyMarryfashionReqCount;
    }
}
