package com.stars.modules.collectphone.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.collectphone.CollectPhoneModule;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class CollectPhoneListenner extends AbstractEventListener<CollectPhoneModule> {
    public CollectPhoneListenner(CollectPhoneModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
