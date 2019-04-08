package com.stars.modules.marry.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.event.EnterSceneEvent;

/**
 * Created by zhouyaohui on 2017/1/20.
 */
public class EnterSceneListener extends AbstractEventListener {
    public EnterSceneListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        EnterSceneEvent ese = (EnterSceneEvent) event;
        if (ese.getSceneType() == SceneManager.SCENETYPE_CITY) {
            MarryModule marryModule = (MarryModule) module();
            marryModule.signCalRedPoint(MConst.Marry, RedPointConst.MARRY_WEDDING);
        }
    }
}
