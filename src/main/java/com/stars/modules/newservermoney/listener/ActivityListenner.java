package com.stars.modules.newservermoney.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.newservermoney.NewServerMoneyModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/23.
 */
public class ActivityListenner implements EventListener {
    NewServerMoneyModule newServerMoneyModule;

    public ActivityListenner(Module module) {
        newServerMoneyModule = (NewServerMoneyModule) module;
    }

    @Override
    public void onEvent(Event event) {
        OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
        switch (operateActivityEvent.getFlag()) {
            case OperateActivityEvent.Flag_Open_Activity: {
                if(newServerMoneyModule.getCurShowActivityId()==operateActivityEvent.getActivityId()) {
                    try {
                        newServerMoneyModule.onDataReq();
                        newServerMoneyModule.onInit(false);
                    } catch (Throwable throwable) {
                        LogUtil.error("天降红包初始化错误", throwable);
                    }
                }
            }
            break;
        }

    }
}
