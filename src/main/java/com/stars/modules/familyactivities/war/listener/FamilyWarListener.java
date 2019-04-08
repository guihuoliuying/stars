package com.stars.modules.familyactivities.war.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.familyactivities.war.FamilyActWarModule;

/**
 * Created by zhaowenshuo on 2016/11/28.
 */
public class FamilyWarListener extends AbstractEventListener<FamilyActWarModule> {

    public FamilyWarListener(FamilyActWarModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
