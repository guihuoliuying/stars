package com.stars.services.levelSpeedUp;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.levelSpeedUp.LevelSpeedUpManager;
import com.stars.modules.levelSpeedUp.event.LevelSpeedUpEvent;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

public class LevelSpeedUpServiceActor implements LevelSpeedUpService{

	@Override
	public void printState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() throws Throwable {
		conditionReset();
	}
	
	@Override
	public void conditionReset(){
		LevelSpeedUpManager.conditionReset();
		try {
			ConcurrentMap<String, AbstractActor> actors = PlayerSystem.system().getActors();
			Iterator<AbstractActor> iterator = actors.values().iterator();
			Actor actor = null;
			Player player = null;
			for(;iterator.hasNext();){
				actor = iterator.next();
				if (actor instanceof Player) {
					player = (Player)actor;
					ServiceHelper.roleService().notice(player.id(), new LevelSpeedUpEvent());
				}
			}
		} catch (Exception e) {
			LogUtil.error("conditionReset notice player fail", e);
		}
	}

}
