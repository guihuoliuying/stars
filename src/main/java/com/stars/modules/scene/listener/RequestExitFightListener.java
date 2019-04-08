package com.stars.modules.scene.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.event.RequestExitFightEvent;

/**
 * 请求离开战斗的监听器;
 * Created by panzhenfeng on 2016/10/25.
 */
public class RequestExitFightListener extends AbstractEventListener<Module> {

    public RequestExitFightListener(Module tm){
        super(tm);
    }
    @Override
    public void onEvent(Event event) {
        if (event instanceof RequestExitFightEvent){
            SceneModule sceneModule = (SceneModule)this.module();
            sceneModule.backToCity();
        }
    }
}