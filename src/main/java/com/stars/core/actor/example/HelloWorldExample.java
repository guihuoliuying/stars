package com.stars.core.actor.example;

import com.stars.core.actor.Actor;
import com.stars.util.log.CoreLogger;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2015/2/4.
 */
public class HelloWorldExample {

    public static void main(String[] args) throws InterruptedException {
        CoreLogger.init("actorexanmle");
        com.stars.core.actor.ActorSystem system = new com.stars.core.actor.ActorSystem(); // 创建一个线程池，用于承载Actor
        HelloWorldActor actor = new HelloWorldActor(); // 创建Actor，收到消息就打印“Hello World！”
        system.addActor(1, actor); // 往线程池添加Actor

        actor.tell(new Object(), actor); // 往这个Actor发送消息

        TimeUnit.SECONDS.sleep(4);
        system.shutdownNow();
    }

}

class HelloWorldActor extends com.stars.core.actor.AbstractActor {
    @Override
    public void onReceived(Object message, Actor sender) { // 接收到消息后的处理方法
        System.out.println("Hello World!");
        stop();
    }

    @Override
    protected void postStop() {
        System.out.println("I'm quit");
    }
}