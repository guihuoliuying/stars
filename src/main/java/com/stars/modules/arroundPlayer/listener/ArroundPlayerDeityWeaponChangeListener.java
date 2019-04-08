package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;

/**
 * Created by panzhenfeng on 2016/12/12.
 */
public class ArroundPlayerDeityWeaponChangeListener extends AbstractEventListener<ArroundPlayerModule> {

    public ArroundPlayerDeityWeaponChangeListener(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().doDeityWeaponChangeEvent((DeityWeaponChangeEvent) event);
    }
}