package com.stars.modules.luckydraw.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckydraw.LuckyDrawModule;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawListenner extends AbstractEventListener<LuckyDrawModule> {
    public LuckyDrawListenner(LuckyDrawModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
