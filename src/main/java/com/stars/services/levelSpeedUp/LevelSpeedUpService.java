package com.stars.services.levelSpeedUp;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;

public interface LevelSpeedUpService extends ActorService, Service{
	
	public void conditionReset();

}
