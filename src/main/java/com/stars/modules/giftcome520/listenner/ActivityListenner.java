package com.stars.modules.giftcome520.listenner;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.core.module.Module;
import com.stars.modules.giftcome520.GiftComeModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/23.
 */
public class ActivityListenner implements EventListener {
    GiftComeModule giftComeModule;

    public ActivityListenner(Module module) {
        giftComeModule = (GiftComeModule) module;
    }

    @Override
    public void onEvent(Event event) {
        OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
        switch (operateActivityEvent.getFlag()) {
            case OperateActivityEvent.Flag_Open_Activity: {
                if(giftComeModule.getCurShowActivityId()==operateActivityEvent.getActivityId()) {
                    try {
                        giftComeModule.onDataReq();
                        giftComeModule.onInit(false);
                    } catch (Throwable throwable) {
                        LogUtil.error("礼尚往来初始化错误", throwable);
                    }
                }
            }
            break;
        }

    }
}
