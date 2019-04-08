package com.stars.modules.scene.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.scene.SceneModule;

/**
 * 回主服时间监听器
 * Created by zhouyaohui on 2016/11/16.
 */
public class BackMainServerEventListener extends AbstractEventListener {

    public BackMainServerEventListener(Module module) {
        super(module);
    }


    @Override
    public void onEvent(Event event) {
        SceneModule sceneModule = (SceneModule) module();
        sceneModule.backToCity(false);
    }
}
