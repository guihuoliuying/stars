package com.stars.modules.newredbag.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.newredbag.NewRedbagModule;

/**
 * Created by zhouyaohui on 2017/2/20.
 */
public class LoginSuccessListener extends AbstractEventListener {
    public LoginSuccessListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        NewRedbagModule module = (NewRedbagModule) module();
        module.onlineForService();
    }
}
