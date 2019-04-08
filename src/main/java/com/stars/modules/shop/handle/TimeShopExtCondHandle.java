package com.stars.modules.shop.handle;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.push.PushModule;
import com.stars.modules.shop.ExtendConditionHandle;
import com.stars.modules.shop.prodata.Shop;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/29.
 */
public class TimeShopExtCondHandle implements ExtendConditionHandle {

    private int pushId;

    @Override
    public void setShopVo(Shop item) {
        this.pushId = item.getGoodsId();
    }

    @Override
    public boolean check(Map<String, Module> moduleMap) {
        PushModule pushModule = (PushModule) moduleMap.get(MConst.Push);
        return pushModule.satisfyDate(pushId);
    }
}
