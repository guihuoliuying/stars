package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.CampCityChangeEvent;

/**
 * Created by huwenjun on 2017/7/3.
 */
public class CampCityChangeListenner extends AbstractEventListener<CampModule> {
    public CampCityChangeListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        CampCityChangeEvent campCityChangeEvent = (CampCityChangeEvent) event;
        module().onChangeCity(campCityChangeEvent);
    }
}
