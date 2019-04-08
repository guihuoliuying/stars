package com.stars.modules.familyEscort.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.familyEscort.FamilyEscortModule;

/**
 * Created by zhaowenshuo on 2017/4/19.
 */
public class FamilyEscortListener extends AbstractEventListener<FamilyEscortModule> {

    public FamilyEscortListener(FamilyEscortModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
