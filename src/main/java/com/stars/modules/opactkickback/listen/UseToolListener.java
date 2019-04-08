package com.stars.modules.opactkickback.listen;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.opactkickback.OpActKcikBackModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.UseToolEvent;

/**
 * Created by likang on 2016/12/20.
 */
public class UseToolListener implements EventListener {
	private OpActKcikBackModule module;
	public UseToolListener(OpActKcikBackModule module) {
		this.module = module;
	}
	@Override
	public void onEvent(Event event) {
		if (!(event instanceof UseToolEvent)) {
			return;
		}
		UseToolEvent e = (UseToolEvent) event;
		int itemId = e.getItemId();
		if (itemId != ToolManager.GOLD && itemId != ToolManager.BANDGOLD) {
			return;
		}
		int constum = e.getCount();
		module.handleConsumeEvent(constum);
	}
}
