package com.stars.core.actor.example;

import com.stars.core.actor.ActorSystem;
import com.stars.util.log.CoreLogger;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2015/2/11.
 */
public class Test3 {

    public static void main(String[] args) {
        CoreLogger.init("actortest");
        CoreLogger.info("start...");
        com.stars.core.actor.ActorSystem system = new ActorSystem();
        final TestActor3 actor = new TestActor3();
        system.addActor(actor);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(actor.c);
                }
            }
        }).start();

        actor.tell(new Object(), null);
    }

}

