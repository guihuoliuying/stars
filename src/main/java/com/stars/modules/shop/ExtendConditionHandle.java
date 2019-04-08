package com.stars.modules.shop;

import com.stars.core.module.Module;
import com.stars.modules.shop.prodata.Shop;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/13.
 */
public interface ExtendConditionHandle {
    void setShopVo(Shop item);

    boolean check(Map<String, Module> moduleMap);
}
