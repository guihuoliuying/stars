package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.fashion.event.FashionChangeEvent;

/**
 * Created by gaopeidian on 2016/10/10.
 */
public class ArroundPlayerFashionChangeListener extends AbstractEventListener<ArroundPlayerModule> {

    public ArroundPlayerFashionChangeListener(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
    	module().doFashionChangeEvent((FashionChangeEvent) event);
    }
}
