package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.services.marry.event.MarryProfressEvent;

/**
 * Created by zhouyaohui on 2017/1/18.
 */
public class ProfressEventListener extends AbstractEventListener {
    public ProfressEventListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        MarryProfressEvent mpe = (MarryProfressEvent) event;
        MarryModule marryModule = (MarryModule) module();
        marryModule.updateProfressList(mpe.getProfressList());
        marryModule.signCalRedPoint(MConst.Marry, RedPointConst.MARRY_PROFRESS);
    }
}
