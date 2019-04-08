package com.stars.modules.tool.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.tool.ToolModule;
import com.stars.services.newredbag.AddToolByEvent;

/**
 * Created by zhouyaohui on 2017/2/15.
 */
public class AddToolByEventListener extends AbstractEventListener {
    public AddToolByEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AddToolByEvent e = (AddToolByEvent) event;
        ToolModule module = (ToolModule) module();
        module.addAndSend(e.toolMap(), e.eventType().getCode());
    }
}
