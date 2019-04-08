package com.stars.modules.opactchargescore;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.opactchargescore.listener.ChargeListener;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.Map;

/**
 * Created by likang on 2017/4/12.
 */
public class OpActChargeScoreModuleFactory extends AbstractModuleFactory<OpActChargeScoreModule> {

	public OpActChargeScoreModuleFactory() {
		super(new OpActChargeScorePacketSet());
	}

	@Override
	public OpActChargeScoreModule newModule(long id, Player self, EventDispatcher eventDispatcher,
                                            Map<String, Module> map) {
		return new OpActChargeScoreModule("付费榜", id, self, eventDispatcher, map);
	}

	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		ChargeListener listener = new ChargeListener((OpActChargeScoreModule) module);
		eventDispatcher.reg(VipChargeEvent.class, listener);
	}

}
