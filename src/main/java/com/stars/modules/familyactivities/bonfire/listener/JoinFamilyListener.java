package com.stars.modules.familyactivities.bonfire.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;

/**
 * Created by zhouyaohui on 2016/10/11.
 */
public class JoinFamilyListener extends AbstractEventListener {

    public JoinFamilyListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FamilyAuthUpdatedEvent ue = (FamilyAuthUpdatedEvent) event;
        if (ue.getType() == FamilyAuthUpdatedEvent.TYPE_NEW ||
                ue.getType() == FamilyAuthUpdatedEvent.TYPE_CREATED) {
            ((FamilyBonfireModule) module()).noticeClientBegin();
        }
        if (ue.getFamilyId() == 0) {
            /** 没有家族，通知活动结束*/
            ((FamilyBonfireModule) module()).noticeClientEnd2();
        }
    }
}
