package com.stars.multiserver.fight;

import com.stars.multiserver.fight.handler.FightHandler;
import com.stars.server.main.actor.ActorServer;

/**
 * 
 * fightActor的工厂类
 * @author dengzhou
 *
 */
public class FightActorFactory {

	public static FightActor newFightActor(String fightId, FightHandler handler, FightHandler protoHandler, byte[] initData) {
		FightActor actor = new FightActor(fightId, initData);
		handler.setFightActor(actor);
		actor.setFightHandler(handler);
		actor.setProtoHandler(protoHandler);
		actor.start();
		ActorServer.getActorSystem().addActor(fightId, actor);
		return actor;
	}

}
