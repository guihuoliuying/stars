package com.stars.multiserver.fight.handler;

import com.stars.util.LogUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaowenshuo on 2016/11/21.
 */
public class FightHandlerFactory {

    private static ConcurrentMap<Short, Class<? extends FightHandler>> classMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Short, FightHandler> protoMap = new ConcurrentHashMap<>(); // 原型Map，用于没有对应fightHandler实例时进行回调；比如addFighter()，但是没有找到对应fightActor

    public static void register(short handlerType, Class<? extends FightHandler> handlerClass) throws Throwable {
        FightHandler protoHandler = handlerClass.newInstance();
        classMap.put(handlerType, handlerClass);
        protoMap.put(handlerType, protoHandler);
    }

    public static Class<? extends FightHandler> getHandlerClass(short handlerType) {
        return classMap.get(handlerType);
    }

    public static FightHandler getProtoHandler(short handlerType) {
        return protoMap.get(handlerType);
    }

    public static FightHandler newInstance(short handlerType) {
        try {
            return classMap.get(handlerType).newInstance();
        } catch (Exception e) {
            LogUtil.error("", e);
            return null;
        }
    }

}
