package com.stars.server.main.actor;

import com.stars.core.actor.ActorSystem;

/**
 * Created by zd on 2015/3/4.
 */
public class ActorServer {

    private static com.stars.core.actor.ActorSystem actorSystem;

    public static com.stars.core.actor.ActorSystem getActorSystem() {
        return actorSystem;
    }

    public static void setActorSystem(ActorSystem actorSystem) {
        ActorServer.actorSystem = actorSystem;
    }

}
