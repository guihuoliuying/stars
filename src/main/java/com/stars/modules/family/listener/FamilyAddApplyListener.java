package com.stars.modules.family.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.event.FamilyAddApplyEvent;

/**
 * Created by chenkeyu on 2016/11/30.
 */
public class FamilyAddApplyListener implements EventListener {
    private FamilyModule module;
    public FamilyAddApplyListener(FamilyModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        FamilyAddApplyEvent familyAddApplyEvent = (FamilyAddApplyEvent) event;
        if (familyAddApplyEvent.getApplyIds()==null){
            module.addApplyList(familyAddApplyEvent.getApplyId());
        }else {
            module.addApplyList(familyAddApplyEvent.getApplyIds());
        }
    }
}
