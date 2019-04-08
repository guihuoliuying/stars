package com.stars.modules.familyactivities.bonfire.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.familyactivities.bonfire.FamilyBonfireModule;

/**
 * Created by zhouyaohui on 2016/10/11.
 */
public class LoginSuccessEventListener extends AbstractEventListener {
    public LoginSuccessEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        FamilyBonfireModule fm = (FamilyBonfireModule) module();
        fm.loginSuccessHandle();
    }
}
