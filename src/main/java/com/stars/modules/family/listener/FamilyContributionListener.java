package com.stars.modules.family.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.family.FamilyModule;

/**
 * Created by chenkeyu on 2016/12/1.
 */
public class FamilyContributionListener implements EventListener {
    private FamilyModule module;
    public FamilyContributionListener(FamilyModule module){
        this.module=module;
    }
    @Override
    public void onEvent(Event event) {
        module.getContribution();
    }
}
