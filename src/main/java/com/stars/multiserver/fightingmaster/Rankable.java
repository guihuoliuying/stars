package com.stars.multiserver.fightingmaster;

/**
 * Created by zhouyaohui on 2016/11/22.
 */
public interface Rankable<T extends Rankable> {

    int compare(T other);

}
