package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.AddProsperousEvent;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class ProperousListenner extends AbstractEventListener<CampModule> {
    public ProperousListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AddProsperousEvent addProsperousEvent = (AddProsperousEvent) event;
        if (addProsperousEvent.getProperous() > 0) {
            module().handleProperousAdd(addProsperousEvent.getProperous());
        }
    }
}
