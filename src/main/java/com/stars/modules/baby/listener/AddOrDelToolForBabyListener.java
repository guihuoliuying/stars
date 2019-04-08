package com.stars.modules.baby.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.baby.BabyModule;

/**
 * Created by chenkeyu on 2017-08-03.
 */
public class AddOrDelToolForBabyListener extends AbstractEventListener {
    public AddOrDelToolForBabyListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        BabyModule babyModule = (BabyModule) module();
        babyModule.signRedPoint();
    }
}
