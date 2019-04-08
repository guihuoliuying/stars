package com.stars.modules.tool.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.tool.ToolModule;

/**
 * Created by zhangjiahua on 2016/3/3.
 */
public class PutOnEventListener extends AbstractEventListener<ToolModule> {

    /**
     * 监听穿上装备的事件
     * */
    public PutOnEventListener(ToolModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
//        PutOnEquipEvent putOnEvent = (PutOnEquipEvent)event;
//        module().disPatchPutOnEvent(putOnEvent);
    }
}
