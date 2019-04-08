package com.stars.modules.familyEscort;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.familyEscort.event.FamilyEscortDropEvent;
import com.stars.modules.familyEscort.event.FamilyEscortEnterPKEvent;
import com.stars.modules.familyEscort.event.FamilyEscortFlowEvent;
import com.stars.modules.familyEscort.gm.FamilyEscortGmHandler;
import com.stars.modules.familyEscort.listener.FamilyEscortListener;
import com.stars.modules.familyEscort.prodata.FamilyEscortConfig;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.gm.GmManager;
import com.stars.services.activities.ActConst;

import java.util.Map;

public class FamilyEscortModuleFactory extends AbstractModuleFactory<FamilyEscortModule> {

	public FamilyEscortModuleFactory(){
		super(new FamilyEscortPacketSet());
	}

	@Override
	public FamilyEscortModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
		return new FamilyEscortModule(id, self, eventDispatcher, map);
	}

	@Override
	public void loadProductData() throws Exception {
		FamilyEscortActivityFlow flow = new FamilyEscortActivityFlow();
		flow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(ActConst.ID_FAMILY_ESORT));

		// 整体赋值
		FamilyEscortManager.flow = flow;
		
		FamilyEscortConfig.config.init();
	}

	@Override
	public void init() throws Exception {
		GmManager.reg("fescort", new FamilyEscortGmHandler());
	}

	@Override
	public void registerListener(EventDispatcher eventDispatcher, Module module) {
		eventDispatcher.reg(FamilyEscortFlowEvent.class, new FamilyEscortListener((FamilyEscortModule) module));
		eventDispatcher.reg(FamilyAuthUpdatedEvent.class, new FamilyEscortListener((FamilyEscortModule) module));
		eventDispatcher.reg(FamilyEscortDropEvent.class, new FamilyEscortListener((FamilyEscortModule) module));
		eventDispatcher.reg(FamilyEscortEnterPKEvent.class, new FamilyEscortListener((FamilyEscortModule) module));
		eventDispatcher.reg(ForeShowChangeEvent.class, new FamilyEscortListener((FamilyEscortModule) module));
	}
}
