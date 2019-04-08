package com.stars.modules.gem.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.gem.GemModule;

/**
 * Created by panzhenfeng on 2017/3/15.
 */
public class GemRoleLevelUpListener extends AbstractEventListener<Module> {

    public GemRoleLevelUpListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        GemModule module = (GemModule)this.module();
        module.updateRedPoints();
    }
}
