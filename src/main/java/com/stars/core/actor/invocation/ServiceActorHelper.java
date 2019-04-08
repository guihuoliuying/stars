package com.stars.core.actor.invocation;

import java.lang.reflect.Proxy;

/**
 * Created by zhaowenshuo on 2016/7/5.
 */
public class ServiceActorHelper {

    public static Object newService(com.stars.core.actor.invocation.ServiceActor actor) {
        Class serviceClazz = null;
        Class[] interfaces = actor.getClass().getInterfaces();
        for (Class clazz : interfaces) {
            if (com.stars.core.actor.invocation.ActorService.class.isAssignableFrom(clazz)) {
                serviceClazz = clazz;
                break;
            }
        }
        if (serviceClazz == null) {
            throw new IllegalArgumentException();
        }
        return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), interfaces,
                new ServiceInvocationHandler(actor));
    }

    public static Object newDispatchService(com.stars.core.actor.invocation.ServiceActor[] actors) {
        Class serviceClazz = null;
        Class[] interfaces = actors[0].getClass().getInterfaces();
        for (Class clazz : interfaces) {
            if (com.stars.core.actor.invocation.ActorService.class.isAssignableFrom(clazz)) {
                serviceClazz = clazz;
                break;
            }
        }
        if (serviceClazz == null) {
            throw new IllegalArgumentException();
        }
        return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), interfaces,
                new ServiceDispatchInvocationHandler(actors));
    }

    public static Object newDispatchService(ServiceActor[] actors, Class<? extends ActorService> serviceClass) {
        Class[] interfaces = actors[0].getClass().getInterfaces();
        for (Class clazz : interfaces) {
            if (serviceClass.isAssignableFrom(clazz)) {
                return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), interfaces,
                        new ServiceDispatchInvocationHandler(actors, serviceClass));
            }
        }
        throw new IllegalArgumentException();
    }

}
