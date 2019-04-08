package com.stars.modules.familyactivities.bonfire.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.familyactivities.bonfire.BonfireActivityFlow;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;
import com.stars.modules.familyactivities.bonfire.event.BonFireDropEvent;
import com.stars.modules.familyactivities.bonfire.event.BonfireActEvent;

/**
 * Created by zhouyaohui on 2016/10/11.
 */
public class BonfireActEventListener extends AbstractEventListener<Module> {

    public BonfireActEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FamilyBonfireModule fm = (FamilyBonfireModule) module();
        if(event instanceof BonfireActEvent) {
            BonfireActEvent be = (BonfireActEvent) event;
            if (be.getState() == BonfireActivityFlow.OPEN) {
                fm.noticeClientBegin();
            } else {
                fm.noticeClientEnd();
            }
        }else if(event instanceof BonFireDropEvent){
            BonFireDropEvent bonFireDropEvent = (BonFireDropEvent) event;
            fm.sendFireDropAward(bonFireDropEvent.getMap());
        }
    }

}
