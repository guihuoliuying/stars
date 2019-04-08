package com.stars.modules.activeweapon.listenner;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.activeweapon.ActiveWeaponModule;

/**
 * Created by huwenjun on 2017/6/16.
 */
public class ActiveWeaponListenner extends AbstractEventListener<ActiveWeaponModule> {
    public ActiveWeaponListenner(ActiveWeaponModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().checkState();
    }
}
