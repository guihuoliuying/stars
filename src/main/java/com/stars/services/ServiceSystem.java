package com.stars.services;

import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class ServiceSystem {

    private static ActorSystem system;

    public static void init() {
        synchronized (ServiceSystem.class) {
            if (system != null) {
                throw new IllegalStateException("玩家系统已创建");
            }
            system = new ActorSystem();
        }
    }

    public static void init(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        synchronized (ServiceSystem.class) {
            if (system != null) {
                throw new IllegalStateException("玩家系统已创建");
            }
            system = new ActorSystem(corePoolSize, maximumPoolSize, keepAliveTime, unit);
        }
    }

    public static ActorSystem system() {
        return system;
    }

    public static ServiceActor getOrAdd(String serviceName, ServiceActor player) {
        return (ServiceActor) system.getOrAddActor(serviceName, player);
    }

    public static Actor getOrAddActor(long actorId, Actor actor) {
        return  system.getOrAddActor(actorId, actor);
    }

    public static ServiceActor get(String serviceName) {
        return (ServiceActor) system.getActor(serviceName);
    }

    public static Actor getActor(long actorId){
        if(system == null){
            return null;
        }
        return system.getActor(actorId);
    }

    public static boolean contains(String serviceName) {
        return system.containActor(serviceName);
    }

    public static int size() {
        return system.size();
    }

}
