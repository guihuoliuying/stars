package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.vip.event.VipLevelupEvent;

/**
 * Created by liuyuheng on 2016/12/12.
 */
public class ArroundPlayerVipLevelUpListener extends AbstractEventListener<ArroundPlayerModule> {
    public ArroundPlayerVipLevelUpListener(ArroundPlayerModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().doVipLevelUpEvent((VipLevelupEvent) event);
    }
}
