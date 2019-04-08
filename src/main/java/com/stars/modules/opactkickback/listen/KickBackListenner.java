package com.stars.modules.opactkickback.listen;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.opactkickback.OpActKcikBackModule;

/**
 * Created by huwenjun on 2017/11/29.
 */
public class KickBackListenner extends AbstractEventListener<OpActKcikBackModule> {


    public KickBackListenner(OpActKcikBackModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
