package com.stars.modules.family.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.family.FamilyModule;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyEventListener extends AbstractEventListener<FamilyModule> {

    public FamilyEventListener(FamilyModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().handleEvent(event);
    }

}
