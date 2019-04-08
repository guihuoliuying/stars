package com.stars.modules.arroundPlayer.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.scene.event.EnterSceneEvent;

public class ArroundPlayerEnterSceneListener extends
        AbstractEventListener<ArroundPlayerModule> {
	
	public ArroundPlayerEnterSceneListener(ArroundPlayerModule m){
		super(m);
	}
	@Override
    public void onEvent(Event event) {
		EnterSceneEvent ese = (EnterSceneEvent)event;
		module().doEnterSceneEvent(ese.getSceneType(), ese.getSceneId(),ese.getLastSceneType(),ese.getLastSceneId());
    }
}
