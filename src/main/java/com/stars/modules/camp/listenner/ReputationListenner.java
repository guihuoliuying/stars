package com.stars.modules.camp.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.event.AddReputationEvent;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class ReputationListenner extends AbstractEventListener<CampModule> {
    public ReputationListenner(CampModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AddReputationEvent addProsperousEvent= (AddReputationEvent) event;
        if(addProsperousEvent.getReputation()>0){
            CampModule module = module();
            module.updateReputation();
        }
    }
}
