package com.stars.core.actor.example;

import com.stars.core.actor.AbstractActor;
import com.stars.core.actor.Actor;

/**
 * Created by zhaowenshuo on 2015/2/11.
 */
public class TestActor3 extends AbstractActor {
    public static volatile long c = 0L;
    @Override
    public void onReceived(Object message, Actor sender) {
        c++;
        tell(message, sender);
    }
}