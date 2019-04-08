package com.stars.modules.shop.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.shop.ShopModule;

/**
 * Created by zhaowenshuo on 2017/3/29.
 */
public class ShopListener extends AbstractEventListener<ShopModule> {

    public ShopListener(ShopModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        ForeShowChangeEvent e = (ForeShowChangeEvent) event;
        if (e.getMap().containsKey(ForeShowConst.TIMESHOP)) {
            module().initTimeShop();
        }
    }
}
