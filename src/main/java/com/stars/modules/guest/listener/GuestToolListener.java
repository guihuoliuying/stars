package com.stars.modules.guest.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.redpoint.RedPointConst;

/**
 * Created by zhouyaohui on 2017/1/18.
 */
public class GuestToolListener extends AbstractEventListener {
    public GuestToolListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        GuestModule guestModule = (GuestModule) module();
        guestModule.signCalRedPoint(MConst.Guest, RedPointConst.GUEST_ACTIVE);
        guestModule.signCalRedPoint(MConst.Guest, RedPointConst.GUEST_UPSTAR);
    }
}
