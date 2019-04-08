package com.stars.core.actor.invocation;

import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.Dispatch;
import com.stars.core.actor.invocation.annotation.DispatchAll;
import com.stars.core.actor.invocation.annotation.Timeout;
import com.stars.core.actor.invocation.exception.InvocationException;
import com.stars.core.actor.invocation.exception.ServiceBusyException;
import com.stars.core.actor.invocation.exception.ServiceStopException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/7/28.
 */
public class ServiceDispatchInvocationHandler implements InvocationHandler {

    private com.stars.core.actor.invocation.ServiceActor[] actors;
    private Class serviceClass;
    private int TIMEOUT = 1500;// ms

    public ServiceDispatchInvocationHandler(com.stars.core.actor.invocation.ServiceActor[] actors) {
        init(actors, findServiceClass(actors));
    }

    public ServiceDispatchInvocationHandler(com.stars.core.actor.invocation.ServiceActor[] actors, Class serviceClass) {
        init(actors, serviceClass);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class[] parameterTypes = method.getParameterTypes();

        if (method.getDeclaringClass() != serviceClass) {
        /* 非ActorService接口的则直接调用方法 */
//            System.out.println("直接调用" + method.getName());
            for (com.stars.core.actor.invocation.ServiceActor serviceActor : actors) {
                method.invoke(serviceActor, args);
            }
            return null;

        } else {
        /* ActorService接口的则通过Actor调用方法 */
//            System.out.println("Actor调用" + method.getName());
            if (isDispatchAll(method)) {
                com.stars.core.actor.invocation.InvocationFuture future = new com.stars.core.actor.invocation.InvocationFuture(actors.length);
                if (args != null && args.length > 0 && args[0] instanceof List) {
                    List[] array = divideArgs((List) args[0]);
                    for (int i = 0; i < actors.length; i++) {
                        Object[] newArgs = Arrays.copyOf(args, args.length);
                        newArgs[0] = array[i]; // rewrite the first argument
                        com.stars.core.actor.invocation.InvocationMessage message = new com.stars.core.actor.invocation.InvocationMessage(methodName, parameterTypes, newArgs, future);
                        invoke(actors[i], message);
                    }

                } else {
                    com.stars.core.actor.invocation.InvocationMessage message = new com.stars.core.actor.invocation.InvocationMessage(methodName, parameterTypes, args, future);
                    for (com.stars.core.actor.invocation.ServiceActor serviceActor : actors) {
                        invoke(serviceActor, message);
                    }
                }
                return waitResult(method, future);

            } else {
                com.stars.core.actor.invocation.InvocationFuture future = new com.stars.core.actor.invocation.InvocationFuture();
                com.stars.core.actor.invocation.InvocationMessage message = new com.stars.core.actor.invocation.InvocationMessage(methodName, parameterTypes, args, future);
//                invoke(findServiceActor(args == null ? null : args[0]), message);
                invoke(findServiceActor(getDispatchKey(method, args)), message);
                return waitResult(method, future);
            }
        }
    }

    private void init(com.stars.core.actor.invocation.ServiceActor[] actors, Class serviceClass) {
        Objects.requireNonNull(actors);
        Objects.requireNonNull(serviceClass);
        if (actors.length == 0) {
            throw new IllegalArgumentException("ServiceActor数组不能为空");
        }
        this.actors = actors;
        this.serviceClass = serviceClass;
    }

    private Class findServiceClass(com.stars.core.actor.invocation.ServiceActor[] actors) {
        Class[] interfaces = actors[0].getClass().getInterfaces();
        for (Class clazz : interfaces) {
            if (ActorService.class.isAssignableFrom(clazz)) {
                return clazz;
            }
        }
        return null;
    }

    private boolean isDispatchAll(Method method) {
        return method.getAnnotation(DispatchAll.class) != null;
    }

    private Object getDispatchKey(Method method, Object[] args) {
        Dispatch anno = method.getAnnotation(Dispatch.class);
        if (anno != null) {
            int index = anno.index();
            if (index < 0 || index >= args.length) {
                throw new IllegalArgumentException("Dispatch.index()=" + index + ", args.length=" + args.length);
            }
            return args[index];
        } else {
            return args == null ? null : args[0];
        }
    }

    private com.stars.core.actor.invocation.ServiceActor findServiceActor(Object firstArg) {
        int index = 0;
        if (firstArg == null) {
            index = Math.abs((int) System.currentTimeMillis() % actors.length);
        } else {
            index = Math.abs(firstArg.hashCode() % actors.length); // 取第一个
        }
        return actors[index];
    }

    private List[] divideArgs(List firstArg) {
        List[] array = new List[actors.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = new ArrayList();
        }
        for (Object obj : firstArg) {
            array[Math.abs(obj.hashCode() % actors.length)].add(obj);
        }
        return array;
    }

    private void invoke(ServiceActor actor, InvocationMessage message) throws Throwable {
        try {
            actor.tell(message, Actor.noSender);
        } catch (IllegalStateException e) {
            switch (e.getMessage()) {
                case "Actor already stop":
                    throw new ServiceStopException(e);
                case "Queue is full":
                    throw new ServiceBusyException(e);
            }
        }
    }

    private Object waitResult(Method method, InvocationFuture future) throws Throwable {
        if (method.getAnnotation(AsyncInvocation.class) == null) {
            int timeout = TIMEOUT;
            if (method.getAnnotation(Timeout.class) != null) {
                timeout = method.getAnnotation(Timeout.class).timeout();
            }
            Object result = future.get(timeout, TimeUnit.MILLISECONDS);
            if (result instanceof Throwable) {
                throw new InvocationException((Throwable) result);
            }
            return result;
        }
        return null;
    }



}
