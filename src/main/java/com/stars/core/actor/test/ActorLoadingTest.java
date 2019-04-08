package com.stars.core.actor.test;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;
import com.stars.core.actor.ActorSystem;
import com.stars.util.log.CoreLogger;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zws on 2015/4/29.
 */
public class ActorLoadingTest {

    public static final int ACTOR_NUM = 1;
    public static final int LOOP_TIMES = 1000000;
    public static final int THREAD_NUM = 2;

    public static void main(String[] args) throws InterruptedException {
        CoreLogger.init("actortest");
        CoreLogger.info("初始化Actor...");
        com.stars.core.actor.ActorSystem system = new ActorSystem();
        final com.stars.core.actor.Actor[] arrays = new com.stars.core.actor.Actor[ACTOR_NUM];
        final long[] counters = new long[ACTOR_NUM];
        for (int i = 0; i < ACTOR_NUM; i++) {
            arrays[i] = new Counter(i, counters);
            system.addActor(i, arrays[i]);
        }
        CoreLogger.info("初始化线程...");
        long s = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            new Thread() {
                @Override public void run() {
                    Random r = new Random(System.nanoTime() ^ System.currentTimeMillis());
                    for (int i = 0; i < LOOP_TIMES; i++) {
                        for (int j = 0; j < ACTOR_NUM; j++) {
                            arrays[j%ACTOR_NUM].tell(new Object(), com.stars.core.actor.Actor.noSender);
                        }
//                        try { TimeUnit.MILLISECONDS.sleep(r.nextInt(10)); } catch (Exception e) { }
                    }
                    latch.countDown();
                }
            }.start();
        }
        CoreLogger.info("等待...");
        latch.await();
        long e = System.currentTimeMillis();
        CoreLogger.info("耗时：" + (e - s) / 1000.0 + "s");
        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < ACTOR_NUM; i++) {
            if (((Counter) arrays[i]).get() != LOOP_TIMES * THREAD_NUM) {
                System.out.println(((Counter) arrays[i]).get());
            }
        }
        CoreLogger.info("完成.");
        system.shutdownNow();
    }
}

class Counter extends AbstractActor {

    private int index;
    private long[] counters;

    public Counter(int index, long[] counters) {
        super(new LinkedBlockingQueue<Mail>());
        this.index = index;
        this.counters = counters;
    }

    @Override
    public void onReceived(Object message, Actor sender) {
        counters[index]++;
    }

    public long get() {
        return counters[index];
    }
}
