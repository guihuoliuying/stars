package com.stars.modules.skyrank.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.skyrank.SkyRankModule;

/**
 * 天梯事件监听
 * 
 * @author xieyuejun
 *
 */
public class SkyRankListener extends AbstractEventListener<SkyRankModule> {

	public SkyRankListener(SkyRankModule module) {
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		module().onEvent(event);
	}
}
