package com.stars.modules.ride.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.ride.RideConst;
import com.stars.modules.ride.RideModule;
import com.stars.modules.ride.event.RideLevelUpEvent;

/**
 * Created by chenkeyu on 2016/11/28.
 */
public class RideLevelUpListener implements EventListener {
    private RideModule module;
    public RideLevelUpListener(RideModule module){this.module=module;}
    @Override
    public void onEvent(Event event) {
        RideLevelUpEvent rideLevelUpEvent = (RideLevelUpEvent) event;
        if(rideLevelUpEvent.getCurrLevelId()== RideConst.DEFAULT_LEVEL){
//            module.addList();
        }else{
            module.removeList();
        }
        module.autoActiveByLvUp(rideLevelUpEvent.getCurrLevelId());
    }
}
