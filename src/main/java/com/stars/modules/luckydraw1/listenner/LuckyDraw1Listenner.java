package com.stars.modules.luckydraw1.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckydraw1.LuckyDraw1Module;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw1Listenner extends AbstractEventListener<LuckyDraw1Module> {
    public LuckyDraw1Listenner(LuckyDraw1Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
