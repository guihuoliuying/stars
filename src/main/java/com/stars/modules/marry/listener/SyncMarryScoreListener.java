package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.marry.event.SyncMarryScoreToOtherEvent;

/**
 * Created by chenkeyu on 2017-07-06.
 */
public class SyncMarryScoreListener extends AbstractEventListener {
    public SyncMarryScoreListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        SyncMarryScoreToOtherEvent otherEvent = (SyncMarryScoreToOtherEvent) event;
        MarryModule marry = (MarryModule) module();
        marry.doMarryScoreEvent(otherEvent.getOther(), otherEvent.getScore());
    }
}
