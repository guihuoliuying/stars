package com.stars.modules.tool.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.GMDelToolEvent;

/**
 * Created by zhoujin on 2017/3/22.
 */
public class GMDelToolListener extends AbstractEventListener {
    public GMDelToolListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GMDelToolEvent) {
        	GMDelToolEvent e = (GMDelToolEvent) event;
        	ToolModule module = (ToolModule) module();
        	module.gmDeleteItem(e.getToolid(), e.getAmount());
        }
    }
}
