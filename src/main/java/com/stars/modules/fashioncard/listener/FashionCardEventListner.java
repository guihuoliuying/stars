package com.stars.modules.fashioncard.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.fashioncard.FashionCardModule;

/**
 * Created by chenkeyu on 2017-10-18.
 */
public class FashionCardEventListner extends AbstractEventListener {
    public FashionCardEventListner(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FashionCardModule cardModule = (FashionCardModule) module();
        FashionChangeEvent changeEvent = (FashionChangeEvent) event;
        if (changeEvent.getCurFashionId() != -1) {
            cardModule.takeOffFashionCard();
        }
    }
}
