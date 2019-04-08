package com.stars.modules.refine.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class AddOrDelToolListener extends AbstractEventListener {
    public AddOrDelToolListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
//        RefineModule refine = (RefineModule) module();
//        if (event instanceof UseToolEvent) {
//            refine.doToolChangeEvent(((UseToolEvent) event).getItemId());
//        }
//        if (event instanceof AddToolEvent) {
//            refine.doToolChangeEvent(((AddToolEvent) event).getToolMap().keySet());
//        }
    }
}
