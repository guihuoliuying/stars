package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.marry.event.MarrySceneFinishEvent;

/**
 * Created by chenkeyu on 2017-07-06.
 */
public class MarrySceneFinishListener extends AbstractEventListener {
    public MarrySceneFinishListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryModule marry = (MarryModule) module();
        marry.finishReward((MarrySceneFinishEvent) event);
    }
}
