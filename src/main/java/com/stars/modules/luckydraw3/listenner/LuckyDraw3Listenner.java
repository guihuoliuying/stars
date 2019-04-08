package com.stars.modules.luckydraw3.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckydraw3.LuckyDraw3Module;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw3Listenner extends AbstractEventListener<LuckyDraw3Module> {
    public LuckyDraw3Listenner(LuckyDraw3Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
