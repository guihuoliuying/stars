package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.services.marry.event.MarryEvent;

/**
 * Created by zhouyaohui on 2017/1/20.
 */
public class MarryEventListener extends AbstractEventListener {
    public MarryEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryEvent me = (MarryEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.updateMarry(me.getMarry());
        marryModule.signCalRedPoint(MConst.Marry, RedPointConst.MARRY_PROFRESS);
    }
}
