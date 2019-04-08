package com.stars.modules.opactsecondskill.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.opactsecondskill.OpActSecondKillModule;


/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class OpActSecondKillListener extends AbstractEventListener<OpActSecondKillModule> {

    public OpActSecondKillListener(OpActSecondKillModule module) {
        super(module);
    }



    @Override
    public void onEvent(Event event) {
        module().onEvent(event);
    }
}
