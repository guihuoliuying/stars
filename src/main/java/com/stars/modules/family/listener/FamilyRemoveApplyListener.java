package com.stars.modules.family.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.event.FamilyRemoveApplyEvent;

/**
 * Created by chenkeyu on 2016/11/30.
 */
public class FamilyRemoveApplyListener implements EventListener {
    private FamilyModule module;

    public FamilyRemoveApplyListener(FamilyModule module) {
        this.module = module;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof FamilyRemoveApplyEvent) {
            module.removeApplyList(((FamilyRemoveApplyEvent) event).getApplyId());
        }
    }
}
