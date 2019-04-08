package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.OfficerChangeEvent;

/**
 * Created by huwenjun on 2017/7/10.
 */
public class OfficerChangeListenner extends AbstractEventListener<CampModule> {
    public OfficerChangeListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof OfficerChangeEvent) {
            module().onOfficerChange((OfficerChangeEvent) event);
        }
    }
}
