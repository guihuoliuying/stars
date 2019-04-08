package com.stars.modules.deityweapon.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.tool.event.AddToolEvent;

public class DeityWeaponListener extends AbstractEventListener<DeityWeaponModule> {

	public DeityWeaponListener(DeityWeaponModule module) {
		super(module);
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof AddToolEvent){
			module().updateForgeRedPionts();
		}
	}

}
