package com.stars.core.module;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;

/**
 * Created by zws on 2015/11/30.
 */
public class AbstractEventListener<T extends Module> implements EventListener {

    private T module;

    public AbstractEventListener(T module) {
        this.module = module;
    }

    public T module() {
        return module;
    }

    @Override
    public void onEvent(Event event) {

    }
}
