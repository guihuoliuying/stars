package com.stars.modules.luckydraw2.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.luckydraw2.LuckyDraw2Module;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw2Listenner extends AbstractEventListener<LuckyDraw2Module> {
    public LuckyDraw2Listenner(LuckyDraw2Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
