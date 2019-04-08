package com.stars.multiserver.fight;

import com.stars.server.main.actor.ActorServer;
import com.stars.util.LogUtil;
import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2017/3/9.
 */
public class FightFrameTimer extends Thread {

    private long gcTimestamp = System.currentTimeMillis();

    @Override
    public void run() {
        FightFrameTick tick = new FightFrameTick();
        while (true) {
            ConcurrentMap<String, AbstractActor> fightMap = ActorServer.getActorSystem().getActors();
            for (Actor fightActor : fightMap.values()) {
                try {
                    fightActor.tell(tick, Actor.noSender);
                } catch (Throwable cause) {
                    FightActor fightActor1 = (FightActor) fightActor;
                    LogUtil.info("run|exception|fightId:{}", fightActor1.getFightId());
                    LogUtil.error(cause.getMessage(), cause);
                }
            }
            try {
                if (System.currentTimeMillis() - gcTimestamp > 120_000) {
                    gcTimestamp = System.currentTimeMillis();
                    System.gc(); // 定时GC
                }
                TimeUnit.MILLISECONDS.sleep(16);
            } catch (Throwable cause) {
                LogUtil.error(cause.getMessage(), cause);
            }
        }
    }
}
