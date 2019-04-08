package com.stars.core.actor.invocation;

import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.exception.InvocationException;
import com.stars.core.actor.invocation.exception.ServiceBusyException;
import com.stars.core.actor.invocation.exception.ServiceStopException;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/6/14.
 */
public class ServiceInvocationHandler implements InvocationHandler {

    private com.stars.core.actor.invocation.ServiceActor actor;
    private Class serviceClass;

    public ServiceInvocationHandler(ServiceActor actor) {
        this.actor = actor;
        this.serviceClass = null;
        Class[] interfaces = actor.getClass().getInterfaces();
        for (Class clazz : interfaces) {
            if (ActorService.class.isAssignableFrom(clazz)) {
                serviceClass = clazz;
                break;
            }
        }
        if (serviceClass == null) {
            throw new IllegalArgumentException("Actor没有实现Service接口");
        }
        for (Method method : serviceClass.getMethods()) {
            if (method.getAnnotation(AsyncInvocation.class) != null
                    && method.getReturnType() != Void.TYPE) {
                throw new WrongMethodTypeException();
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() != serviceClass) {
        /* 非ActorService接口的则直接调用方法 */
//            System.out.println("直接调用" + method.getName());
            return method.invoke(actor, args);

        } else {
        /* ActorService接口的则通过Actor调用方法 */
//            System.out.println("Actor调用" + method.getName());
            com.stars.core.actor.invocation.InvocationFuture future = new InvocationFuture();
            com.stars.core.actor.invocation.InvocationMessage message = new InvocationMessage(method.getName(), method.getParameterTypes(), args, future);
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

            if (method.getAnnotation(AsyncInvocation.class) == null) {
                Object result = future.get(1500, TimeUnit.MILLISECONDS);
                if (result instanceof Throwable) {
                    throw new InvocationException((Throwable) result);
                }
                return result;
            }
        }
        return null;
    }
}
