package com.stars.modules.guest.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.guest.GuestModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.event.EnterSceneEvent;

/**
 * Created by zhouyaohui on 2017/1/18.
 */
public class EnterSceneListener extends AbstractEventListener {
    public EnterSceneListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        EnterSceneEvent ee = (EnterSceneEvent) event;
        if (ee.getSceneType() == SceneManager.SCENETYPE_CITY) {
            GuestModule module = (GuestModule) module();
            module.signCalRedPoint(MConst.Guest, RedPointConst.GUEST_MISSION_FINISH);
        }
    }
}
