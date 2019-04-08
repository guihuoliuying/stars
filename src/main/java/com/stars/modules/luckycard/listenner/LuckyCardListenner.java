package com.stars.modules.luckycard.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckycard.LuckyCardModule;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCardListenner extends AbstractEventListener<LuckyCardModule> {

    public LuckyCardListenner(LuckyCardModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
