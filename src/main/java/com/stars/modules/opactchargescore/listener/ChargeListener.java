package com.stars.modules.opactchargescore.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.opactchargescore.OpActChargeScoreModule;
import com.stars.modules.vip.event.VipChargeEvent;

/**
 * Created by likang on 2017/4/12.
 */

public class ChargeListener extends AbstractEventListener<OpActChargeScoreModule> {

	public ChargeListener(OpActChargeScoreModule module) {
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof VipChargeEvent) {
			VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
			module().handleChargeEvent(vipChargeEvent.getMoney());
		}
	}
}
