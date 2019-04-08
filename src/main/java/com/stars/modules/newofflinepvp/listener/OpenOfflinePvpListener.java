package com.stars.modules.newofflinepvp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.newofflinepvp.NewOfflinePvpModule;

/**
 * Created by chenkeyu on 2017-03-10 10:54
 */
public class OpenOfflinePvpListener extends AbstractEventListener<NewOfflinePvpModule> {


    public OpenOfflinePvpListener(NewOfflinePvpModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        ForeShowChangeEvent openEvent = (ForeShowChangeEvent) event;
//        LogUtil.info("竞技场是否开放:{}",openEvent.getMap().containsKey(ForeShowConst.OfflinePvp));
        if (openEvent.getMap().containsKey(ForeShowConst.OfflinePvp)){
            module().openOfflinePvp();
        }
    }
}
