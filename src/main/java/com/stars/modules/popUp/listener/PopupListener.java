package com.stars.modules.popUp.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.popUp.PopUpModule;

/**
 * Created by zhaowenshuo on 2017/7/10.
 */
public class PopupListener extends AbstractEventListener<PopUpModule> {
    public PopupListener(PopUpModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
