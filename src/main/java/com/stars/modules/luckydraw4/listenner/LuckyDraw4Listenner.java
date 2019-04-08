package com.stars.modules.luckydraw4.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckydraw4.LuckyDraw4Module;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw4Listenner extends AbstractEventListener<LuckyDraw4Module> {
    public LuckyDraw4Listenner(LuckyDraw4Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
