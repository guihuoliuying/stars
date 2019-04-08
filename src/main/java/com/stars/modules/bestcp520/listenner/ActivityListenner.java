package com.stars.modules.bestcp520.listenner;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.bestcp520.BestCPModule;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/23.
 */
public class ActivityListenner implements EventListener {
    BestCPModule bestCPModule;

    public ActivityListenner(Module module) {
        bestCPModule = (BestCPModule) module;
    }

    @Override
    public void onEvent(Event event) {
        try {
            bestCPModule.onDataReq();
            bestCPModule.onInit(false);
        } catch (Throwable throwable) {
            LogUtil.error("最佳组合初始化错误", throwable);
        }

    }
}
