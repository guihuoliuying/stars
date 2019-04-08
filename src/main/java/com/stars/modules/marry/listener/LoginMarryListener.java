package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.marry.MarryModule;

/**
 * Created by zhouyaohui on 2016/12/23.
 */
public class LoginMarryListener extends AbstractEventListener {
    public LoginMarryListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryModule marryModule = (MarryModule) module();
//        marryModule.playerOnline();
    }
}
