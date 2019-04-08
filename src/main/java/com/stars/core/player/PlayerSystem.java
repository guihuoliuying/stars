package com.stars.core.player;

import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/2/3.
 */
public class PlayerSystem {

    private static ActorSystem system;
    private static ConcurrentMap<String, Long> account2IdMap = new ConcurrentHashMap<>(2048);
    private static int actorCount = 3000;

    public static void init() {
        synchronized (PlayerSystem.class) {
            if (system != null) {
                throw new IllegalStateException("玩家系统已创建");
            }
            system = new ActorSystem();
        }
    }

    public static void init(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        synchronized (PlayerSystem.class) {
            if (system != null) {
                throw new IllegalStateException("玩家系统已创建");
            }
            system = new ActorSystem(corePoolSize, maximumPoolSize, keepAliveTime, unit);
        }
    }

    public static ActorSystem system() {
        return system;
    }

    public static ConcurrentMap<String, Long> account2IdMap() {
        return account2IdMap;
    }

    public static Player getOrAdd(long roleId, Player player) {
        if(size()+1>actorCount){
            return null;
        }
        return (Player) system.getOrAddActor(roleId, player);
    }

    public static Actor getOrAddActor(long actorId, Actor actor) {
        return  system.getOrAddActor(actorId, actor);
    }

    public static Player get(long roleId) {
        return (Player) system.getActor(roleId);
    }

    public static Actor getActor(long actorId){
        if(system == null){
            return null;
        }
        return system.getActor(actorId);
    }

    public static boolean contains(long roleId) {
        return system.containActor(roleId);
    }

    public static int size() {
        return system.size();
    }

    public static int getActorCount() {
        return actorCount;
    }

    public static void setActorCount(int actorCount) {
        PlayerSystem.actorCount = actorCount;
    }
}
