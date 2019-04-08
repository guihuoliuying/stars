package com.stars.modules.role.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.ModifyRoleLevelEvent;

/**
 * Created by liuyuheng on 2017/2/16.
 */
public class ModifyRoleLevelListener extends AbstractEventListener<RoleModule> {
    public ModifyRoleLevelListener(RoleModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ModifyRoleLevelEvent) {
            module().gmModifyLevelHandler(((ModifyRoleLevelEvent) event).getValue());
        }
    }
}
