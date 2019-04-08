package com.stars.modules.foreshow.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.foreshow.ForeShowModule;

/**
 * Created by chenkeyu on 2016/10/31.
 */
public class ForeShowChangeListener extends AbstractEventListener<ForeShowModule> {

    public ForeShowChangeListener(ForeShowModule module) {
        super(module);
    }
    @Override
    public void onEvent(Event event){
        module().updateForeShow();
    }
}
