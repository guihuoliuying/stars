package com.stars.core.actor;

/**
 * Created by zhaowenshuo on 2015/3/3.
 */
public interface DeadMessageHandler {

    void handle(Object message, Actor sender);

}
