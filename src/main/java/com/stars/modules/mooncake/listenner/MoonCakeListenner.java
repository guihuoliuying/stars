package com.stars.modules.mooncake.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.mooncake.MoonCakeModule;

/**
 * Created by huwenjun on 2017/11/29.
 */
public class MoonCakeListenner extends AbstractEventListener<MoonCakeModule> {
    public MoonCakeListenner(MoonCakeModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
