package com.stars.core.actor;


import com.stars.util.LogUtil;

import java.util.concurrent.*;

/**
 * Created by zhaowenshuo on 2014/12/29.
 */
public class ActorSystem {


    private ThreadPoolExecutor pool; // 线程池
    private ConcurrentMap<String, AbstractActor> actors = new ConcurrentHashMap<>(); // 已添加的actor集合

    public ActorSystem() {
        this(Runtime.getRuntime().availableProcessors() * 2,
                Runtime.getRuntime().availableProcessors() * 2, 60, TimeUnit.SECONDS);
    }

    public ActorSystem(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>());
        pool.prestartAllCoreThreads();
    }

    /**
     * 添加actor
     *
     * @param actor actor
     */
    public Actor addActor(Actor actor) {
        return addActor(null, actor);
    }

    public Actor addActor(long id, Actor actor) {
        return addActor(Long.toString(id), actor);
    }

    public Actor addActor(String name, Actor actor) {
        if (actor == null) {
            throw new NullPointerException("Actor can not be null");
        }
        if (!(actor instanceof AbstractActor)) {
            throw new ClassCastException("Actor must be a instance of AbstractActor");
        }
        if (name != null && actor.getName() != null) {
            throw new IllegalArgumentException("Actor already had a name: " + actor.getName());
        }
        // add actor into system
        AbstractActor newActor = (AbstractActor) actor;
        // set the actor's name
        if (newActor.getName() == null) {
            newActor.setName(name != null ? name : newActor.toString());
        }
        newActor.setSystem(this);
        Actor oldActor = actors.putIfAbsent(newActor.getName(), newActor);
        if (oldActor == null) {
            LogUtil.info("actor: system add an actor {}", newActor.getName());
            return newActor;
        }
        return oldActor;
    }

    public Actor getOrAddActor(long id, Actor actor) {
        return getOrAddActor(Long.toString(id), actor);
    }

    public Actor getOrAddActor(String name, Actor newActor) {
        Actor oldActor = actors.get(name);
        if (oldActor != null) {
            return oldActor;
        }
        oldActor = addActor(name, newActor);
        return oldActor;
    }

    /**
     * 移除actor
     *
     * @param key actor
     */
    public void removeActor(Actor key) {
        removeActor(key.getName());
    }

    public void removeActor(long id) {
        removeActor(Long.toString(id));
    }

    public void removeActor(String name) {
        actors.remove(name);
//        CoreLogger.trace("actor: system remove an actor {}", name);
    }

    public boolean containActor(Actor key) {
        return containActor(key.getName());
    }

    public boolean containActor(long id) {
        return containActor(Long.toString(id));
    }

    public boolean containActor(String name) {
        return actors.containsKey(name);
    }

    public Actor getActor(long id) {
        return actors.get(Long.toString(id));
    }

    public Actor getActor(String name) {
        return actors.get(name);
    }

    public boolean isEmpty() {
        return actors.isEmpty();
    }

    public int size() {
        return actors.size();
    }

    /**
     * 提交任务
     *
     * @param task 任务
     */
    void submit(Runnable task) {
        pool.submit(task);
//        CoreLogger.trace("actor: system receive a task {}", task);
    }

    /**
     * 关掉线程池
     */
    public void shutdownNow() {
//        CoreLogger.trace("actor: system start to shutdown");
        pool.shutdown();
        actors.clear();
//        CoreLogger.trace("actor: system shutdown");
    }

    public ConcurrentMap<String, AbstractActor> getActors() {
        return actors;
    }
}
