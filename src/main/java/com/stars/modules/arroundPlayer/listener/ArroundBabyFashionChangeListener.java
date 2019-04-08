package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.baby.event.BabyFashionChangeEvent;

/**
 * Created by gaopeidian on 2016/10/10.
 */
public class ArroundBabyFashionChangeListener extends AbstractEventListener<ArroundPlayerModule> {

    public ArroundBabyFashionChangeListener(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().doBabyFashionChangeEvent((BabyFashionChangeEvent) event);
    }
}
