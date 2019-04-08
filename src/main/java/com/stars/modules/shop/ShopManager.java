package com.stars.modules.shop;

import com.stars.modules.shop.prodata.Shop;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhouyaohui on 2016/9/5.
 */
public class ShopManager {

    public static final int SHOPTYPE_1 = 1; // 限时
    public static final int SHOPTYPE_2 = 2; // 元宝
    public static final int SHOPTYPE_3 = 3; // 绑金
    public static final int SHOPTYPE_4 = 4; // 银币
    public static final int SHOPTYPE_FAMILY = 5; // 家族商店
    public static final int SHOPTYPE_6 = 6; // 荣誉
    public static final int SHOPTYPE_7 = 7; // 装备
    public static final int SHOPTYPE_10 = 10;//精华商店
    public static final int SHOPTYPE_TIMESHOP = 101; // 限时特惠

    public final static String YYMMDDHH = "yyyyMMddHH";
    public final static int DEFAULTCOUNT = 20;  // 随机池个数默认值
    public volatile static int version = 0;  // 产品数据版本号

    /**
     * 商店产品数据map
     * shopMap 所有的产品数据
     * fixedMap 固定商品数据  weight = 0
     * randomMap 随机商品数据 weight != 0
     */
    public static ConcurrentHashMap<Integer, Shop> shopMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Shop> fixedMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Shop> randomMap = new ConcurrentHashMap<>();

    /**
     * shoptype -> extendConfitionhandle
     */
    public static ConcurrentHashMap<Integer, Class<? extends ExtendConditionHandle>> handleMap = new ConcurrentHashMap<>();


    public static void registerHandle(int shopType, Class<? extends ExtendConditionHandle> handle) {
        Objects.requireNonNull(handle, "处理类不能为null");
        handleMap.putIfAbsent(shopType, handle);
    }

}
